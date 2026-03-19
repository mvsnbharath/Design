# Scaling Story: Read-Heavy Configuration Service

Interview walkthrough diagrams

## 1. Baseline

```mermaid
flowchart LR
    A[External Requests<br/>Customer ID] --> D[Service]
    B[Internal Requests<br/>UUID] --> D
    D --> E[Database]
    E --> F[High Read Load]
    F --> G[6x Cost Increase]
    H[50K RPM] --> I[100K RPM]
    I --> G

flowchart LR
    A[Request] --> B[Service]
    B --> C{Partition Key Known?}
    C -->|Yes, but ignored| D[Cross-Partition Query]
    D --> E[Full Scan / High RU]

flowchart LR
    A[Request] --> B[Service]
    B --> C{Partition Key Known?}
    C -->|Yes| D[Point Read]
    C -->|No| E[Query]
    D --> F[Low Cost]
    E --> G[Higher Cost]

flowchart LR
    A[Internal Request<br/>UUID] --> B[Service]
    B --> C[Reverse Index]
    C --> D[Primary ID / Partition Key]
    D --> E[Point Read]
    E --> F[Efficient Internal Path]

flowchart LR
    A[Read Requests] --> B[Service]
    B --> C[Cache]
    C -->|Miss / Expired| D[Database]
    D --> E[High DB Load]

    C --> F[Short TTL]
    C --> G[Long TTL]

    F --> H[Fresh Data]
    F --> I[More DB Reads]

    G --> J[Fewer DB Reads]
    G --> K[More Staleness]

flowchart LR
    A[Read Requests] --> B[Service]
    B --> C[Version Lookup]
    C --> D[Cache v1]
    C --> E[Cache v2]
    C --> F[Cache v3]
    F --> G[Latest Config]
    G --> H[Serve Request]
    H --> I[Fewer Invalidations]
    H --> J[Lower DB Reads]
    K[Tradeoff] --> L[Higher Memory Use]

flowchart LR
    A[External Requests<br/>Customer ID] --> D[Service]
    B[Internal Requests<br/>UUID] --> D

    D --> E[Reverse Index]
    E --> F[Primary ID / Partition Key]

    D --> G[Versioned Cache]
    G -->|Hit| H[Serve Config]
    G -->|Miss| I[Database]

    I --> J[Point Reads]
    J --> G

    K[50K RPM -> 100K RPM] --> L[Optimizations]
    L --> M[DB Cost -70%]
    L --> N[Cache Cost +20%]
    L --> O[Scaled Much Further]