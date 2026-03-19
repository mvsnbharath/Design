# Scaling a Configuration-Heavy Service

A progressive walkthrough of optimizations that reduced DB cost by ~70%.

---

## 1. Baseline Problem

```mermaid
flowchart TB
    subgraph Traffic["Traffic Growth"]
        T1["50K RPM"] -->|2x increase| T2["100K RPM"]
    end

    subgraph Cost["DB Cost Impact"]
        C1["$X"] -->|6x increase| C2["$6X"]
    end

    subgraph Flows["Two Request Flows"]
        direction LR
        EXT["External Requests<br/>customer-friendly ID"]
        INT["Internal Requests<br/>UUID"]
    end

    Traffic --> Cost
    EXT --> DB[(Database)]
    INT --> DB
```

---

## 2. Expensive Query Path

```mermaid
flowchart LR
    subgraph External["External Flow — Efficient"]
        E1[External Request] -->|customer ID = partition key| E2[Point Read] --> DB1[(Database)]
    end

    subgraph Internal["Internal Flow — Expensive"]
        I1[Internal Request] -->|UUID ≠ partition key| I2[Cross-Partition Query] --> DB2[(Database)]
    end
```

---

## 3. Point-Read Optimization

```mermaid
flowchart LR
    R1[Request] --> Check{Partition Key<br/>Known?}
    Check -->|Yes| PR[Point Read<br/>O 1 — cheap] --> DB[(Database)]
    Check -->|No| CQ[Cross-Partition Query<br/>O N — expensive] --> DB
```

---

## 4. Reverse-Index Optimization

```mermaid
flowchart LR
    I1[Internal Request<br/>UUID] --> RI[Reverse Index<br/>UUID → Partition Key]
    RI --> PR[Point Read] --> DB[(Database)]

    E1[External Request<br/>Customer ID] --> PR2[Point Read] --> DB
```

---

## 5. Cache Invalidation Tradeoff

```mermaid
flowchart TB
    subgraph ShortTTL["Short TTL"]
        direction LR
        S1[Fresh Data ✔] --> S2[Too Many DB Reads ✘]
    end

    subgraph LongTTL["Long TTL"]
        direction LR
        L1[Fewer DB Reads ✔] --> L2[Stale Data ✘]
    end

    Service --> Cache[(Cache)]
    Cache --> ShortTTL
    Cache --> LongTTL
```

---

## 6. Versioned Cache Design

```mermaid
flowchart TB
    Service -->|read config| Cache[(Cache)]

    subgraph Cache_Versions["Versioned Cache"]
        direction LR
        V1["v1 (old)"]
        V2["v2 (current)"]
        V3["v3 (latest) ✔"]
    end

    Cache --> Cache_Versions
    Config[Config Change] -->|version bump| V3

    subgraph Tradeoff["Tradeoff"]
        direction LR
        T1["+ Higher Memory<br/>multiple versions coexist"]
        T2["✔ Acceptable<br/>config changes are rare"]
    end
```

---

## 7. Final System + Outcomes

```mermaid
flowchart TB
    EXT[External Requests] --> Service
    INT[Internal Requests] --> Service

    Service --> VC[(Versioned Cache)]
    Service --> RI[Reverse Index]
    RI --> PR[Point Reads Only]
    PR --> DB[(Database)]
    VC -->|cache miss| DB

    subgraph Outcomes["Results"]
        direction LR
        O1["DB Cost: −70%"]
        O2["Cache Cost: +20%"]
        O3["Scale: 100K+ RPM"]
    end
```