# Metrics Export to Customer Storage

## 1. Goal

Deliver platform resource metrics (e.g. Redis cache metrics) from a central cache to cloud customers who have opted in, routing each customer's metrics to their designated storage account.

```mermaid
flowchart TB
    A[Platform Resource Metrics<br/>e.g. Redis Cache Metrics] -->|export| B[Customer Storage Account]
```

---

## 2. Basic Flow

A **Central Producer** emits metrics. The **Data Plane** receives them, calls the **Control Plane** to look up whether an export configuration exists for that resource, retrieves the destination storage account details, and delivers the metrics.

```mermaid
flowchart TB
    subgraph Producer[Central Metrics Producer]
        P[Platform Metrics Emitter]
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

    subgraph Customer[Customer Destination]
        SA[Storage Account]
    end

    P --> D1
    D1 --> D2
    D2 -->|Is there a config for this resource?| CP
    CP -->|Returns storage account details| D3
    D3 --> D4
    D4 --> SA
```

---

## 3. The Problem: Excessive Control Plane Calls

In practice, only a tiny fraction of resources have export enabled — perhaps **10 out of 10,000**. Without any filtering, the Data Plane makes a Control Plane lookup for **every** metric it processes, meaning ~9,990 out of every 10,000 calls return "no config found" and are completely wasted.

```mermaid
flowchart TB
    P[Platform Resource Metrics<br/>e.g. 10,000 Redis instances] --> DP[Data Plane]
    DP -->|10,000 config lookups| CP[Control Plane]
    CP -->|9,990 = no config ❌<br/>10 = has config ✔| DP
```

This creates unnecessary load on the Control Plane and adds latency to the export path.

---

## 4. Solution: Introduce a Bloom Filter

A **Bloom filter** is placed in the Data Plane as a lightweight, in-memory pre-check. Before calling the Control Plane, the Data Plane checks the Bloom filter:

- **Not in filter** → skip the lookup entirely (guaranteed no config exists)
- **In filter** → proceed with the Control Plane call (may be a true positive or a rare false positive)

This eliminates the vast majority of unnecessary calls.

```mermaid
flowchart TB
    subgraph Producer[Platform Resource Metrics Producer]
        P[e.g. Redis Cache Metrics Emitter]
    end

    subgraph DataPlane[Data Plane]
        D1[Receive Metrics]
        BF{Bloom Filter<br/>Check}
        D2[Lookup Config]
        D3[Resolve Destination]
        D4[Deliver Metrics]
    end

    subgraph ControlPlane[Control Plane]
        CP[Configuration Store]
    end

    subgraph Customer[Customer Destination]
        SA[Storage Account]
    end

    P --> D1
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

The Bloom filter must stay in sync with the Control Plane's configuration. Two mechanisms keep it current:

| Mechanism | Frequency | Purpose |
|---|---|---|
| **Incremental Update** | Every 15 minutes | Picks up recent config additions/changes quickly |
| **Full Rebuild** | Daily | Corrects any drift; removes stale entries by rebuilding from scratch |

The Control Plane builds the filter and pushes it to the Data Plane.

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