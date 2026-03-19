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
flowchart TB
    subgraph BF[Bloom Filter — Bit Array]
        direction LR
        B0[0]
        B1[1]
        B2[0]
        B3[1]
        B4[0]
        B5[1]
        B6[0]
        B7[0]
    end

    subgraph Insert[Insert: Resource X]
        H1["Hash₁(X) → index 1"]
        H2["Hash₂(X) → index 3"]
        H3["Hash₃(X) → index 5"]
    end

    subgraph Lookup[Lookup: Resource Y]
        L1["Hash₁(Y) → index 1 → ✔"]
        L2["Hash₂(Y) → index 4 → ✘"]
        L3["Any bit = 0 → Definitely NOT in set"]
    end

    Insert --> BF
    BF --> Lookup
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