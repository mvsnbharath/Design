# Metrics Export to Customer Storage

## 1. Goal

```mermaid
flowchart TB
    A[Platform Resource Metrics<br/>e.g. Redis Cache Metrics] -->|export| B[Customer Storage Account]
```

---

## 2. Basic Flow

```mermaid
flowchart TB
    subgraph Source[Platform Resource Metrics]
        PR[e.g. Redis Cache Metrics]
    end

    subgraph CMS[Central Metrics Store]
        P[Metrics Stream]
    end

    subgraph DataPlane[Data Plane]
        D1[Fork & Receive Metrics]
        D2[Lookup Config]
        D3[Resolve Destination]
        D4[Deliver Metrics]
    end

    subgraph ControlPlane[Control Plane]
        CP[Configuration Store]
    end

    subgraph Customer[Customer Storage Account]
        SA[Storage Account]
    end

    PR -->|emit| P
    P -->|fork| D1
    D1 --> D2
    D2 -->|Is there a config for this resource?| CP
    CP -->|Returns storage account details| D3
    D3 --> D4
    D4 --> SA
```

---

## 3. The Problem: Excessive Control Plane Calls

```mermaid
flowchart TB
    PR[Platform Resource Metrics<br/>e.g. 10,000 Redis instances] -->|emit| CMS[Central Metrics Store]
    CMS -->|fork| DP[Data Plane]
    DP -->|10,000 config lookups| CP[Control Plane]
    CP -->|9,990 = no config ❌<br/>10 = has config ✔| DP
```

---

## 3.5 How a Bloom Filter Works

```mermaid
flowchart LR
    subgraph BF["Bloom Filter — Bit Array"]
        direction LR
        B0["[0]=0"]
        B1["[1]=1"]
        B2["[2]=0"]
        B3["[3]=1"]
        B4["[4]=0"]
        B5["[5]=1"]
        B6["[6]=0"]
        B7["[7]=0"]
    end

    subgraph Insert["Insert: Resource X"]
        H1["Hash₁(X) → index 1"]
        H2["Hash₂(X) → index 5"]
    end

    subgraph Lookup_TP["✔ True Positive — Resource X"]
        T1["Hash₁(X) → index 1 → ✔"]
        T2["Hash₂(X) → index 5 → ✔"]
        T3["All bits = 1 → In set ✔ (X was inserted)"]
    end

    subgraph Lookup_TN["✔ True Negative — Resource Y"]
        L1["Hash₁(Y) → index 1 → ✔"]
        L2["Hash₂(Y) → index 4 → ✘"]
        L3["Any bit = 0 → Definitely NOT in set"]
    end

    subgraph Lookup_FP["⚠ False Positive — Resource Z"]
        F1["Hash₁(Z) → index 1 → ✔"]
        F2["Hash₂(Z) → index 3 → ✔"]
        F3["All bits = 1 → Looks like it's in set, but Z was never inserted!"]
    end

    Insert --> BF
    BF --> Lookup_TP
    BF --> Lookup_TN
    BF --> Lookup_FP
```

```
Insert: hash the resource ID → set bits at those positions to 1.
Lookup: hash the resource ID → check bits.
  • All bits = 1  → Possibly in set (proceed with Control Plane call)
  • Any bit  = 0  → Definitely NOT in set (skip — no config exists)
  • False positives possible, false negatives never.
```

---

## 4. Solution: Introduce a Bloom Filter

```mermaid
flowchart TB
    subgraph Source[Platform Resource Metrics]
        PR[e.g. Redis Cache Metrics]
    end

    subgraph CMS[Central Metrics Store]
        P[Metrics Stream]
        BF{Bloom Filter<br/>Check}
    end

    subgraph DataPlane[Data Plane]
        D1[Receive Metrics]
        D2[Lookup Config]
        D3[Resolve Destination]
        D4[Deliver Metrics]
    end

    subgraph ControlPlane[Control Plane]
        CP[Configuration Store]
    end

    subgraph Customer[Customer Storage Account]
        SA[Storage Account]
    end

    PR -->|emit| P
    P --> BF
    BF -->|Not in filter → skip| P
    BF -->|Possibly in filter → forward| D1
    D1 --> D2
    D2 --> CP
    CP --> D3
    D3 --> D4
    D4 --> SA
```

---

## 5. Managing the Bloom Filter

```mermaid
flowchart TB
    subgraph ControlPlane[Control Plane]
        CS[Configuration Store]
        BFB[Bloom Filter Builder]
        INC[Incremental Update<br/>every 15 min]
        FULL[Full Rebuild<br/>daily]
    end

    subgraph CMS[Central Metrics Store]
        BF[Bloom Filter]
    end

    CS --> BFB
    BFB --> INC --> BF
    BFB --> FULL --> BF
```