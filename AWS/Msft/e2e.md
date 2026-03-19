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

    subgraph Insert_X["Insert: Resource X"]
        direction LR
        H1["Hash₁(X) → index 1"]
        H2["Hash₂(X) → index 5"]
    end

    subgraph Insert_W["Insert: Resource W"]
        direction LR
        H3["Hash₁(W) → index 3"]
        H4["Hash₂(W) → index 5"]
    end

    Insert_X --> BF
    Insert_W --> BF
```

```mermaid
flowchart LR
    subgraph Lookup_TP["✔ True Positive — Resource X"]
        direction LR
        T1["Hash₁(X) → [1]=1 ✔"] --> T2["Hash₂(X) → [5]=1 ✔"] --> T3["All bits = 1 → In set ✔"]
    end
```

```mermaid
flowchart LR
    subgraph Lookup_TN["✔ True Negative — Resource Y"]
        direction LR
        L1["Hash₁(Y) → [1]=1 ✔"] --> L2["Hash₂(Y) → [4]=0 ✘"] --> L3["A bit = 0 → NOT in set"]
    end
```

```mermaid
flowchart LR
    subgraph Lookup_FP["⚠ False Positive — Resource Z"]
        direction LR
        F1["Hash₁(Z) → [1]=1 ✔"] --> F2["Hash₂(Z) → [3]=1 ✔"] --> F3["All bits = 1, but Z was never inserted!"]
    end
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