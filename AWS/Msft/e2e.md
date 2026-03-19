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

## 4. Solution: Introduce a Bloom Filter

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
        BF{Bloom Filter<br/>Check}
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
    D1 --> BF
    BF -->|Not in filter → skip| D1
    BF -->|Possibly in filter → proceed| D2
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

    subgraph DataPlane[Data Plane]
        BF[Bloom Filter]
    end

    CS --> BFB
    BFB --> INC --> BF
    BFB --> FULL --> BF
```