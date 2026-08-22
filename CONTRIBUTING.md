# Contributing to MaceControl

Thanks for wanting to contribute! Here's how to get started.

## Prerequisites

- JDK 21
- Maven 3.8+
- A Paper 1.21.11 test server (optional but recommended)

## Building

```bash
git clone https://github.com/vulgarmc/MaceControl.git
cd MaceControl
mvn package
```

The compiled JAR will be at `target/MaceControl-1.0.0.jar`.

## Submitting a PR

1. Fork the repo and create a branch off `main`.
2. Make your changes — please keep the vanilla-default comments in `config.yml` accurate.
3. Open a pull request with a clear description of what changed and why.

## Reporting bugs

Open an issue and include:
- Your Paper build version (from server console startup line)
- Your `config.yml`
- What you expected vs what happened
