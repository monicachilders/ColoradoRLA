# Colorado Risk-Limiting Audit (RLA) Tool

[![Build Status](https://travis-ci.org/democracyworks/ColoradoRLA.svg?branch=master)](https://travis-ci.org/democracyworks/ColoradoRLA)

The **Colorado RLA** system is software to facilitate risk-limiting audits at
the state level, developed for Colorado's Department of State in July and August
of 2017.

## What is a risk-limiting audit?

A [risk-limiting audit](https://en.wikipedia.org/wiki/Risk-limiting_audit) is an
audit of the results of an election which uses statistical methods to give high
confidence that the correct winners of the election were identified. This
method, under most circumstances, requires far fewer ballots to be audited than
a full hand count in which auditors must check every ballot.

# Docker Quick Start

Primarily used to spin up the system for development in a controlled way. This
is a work in progress (automating the artifact build steps would be nice), but
is usable.

## Requirements

- `docker`
- `docker-compose`
- Build tools for the client and server (TypeScript, maven, etc.)

## Setup

```sh
## Build the client
cd client; npm install; npm run dist
## Switch back to project root
cd ..
## Build the server
cd server/eclipse-project; mvn package
```

## Running

Assuming you have built artifacts, you can bring up the system with those
artifacts:

```sh
## This step is optional the first time, but you need to run it subsequently
## whenever artifacts change (refer to the **Setup** instructions above).
docker-compose build

docker-compose up
```

Once the system is running, the server will create the PostgreSQL schema. After
this, you most likely want to install test credentials, which are already inside
the PostgreSQL image:

```sh
docker exec -i coloradorla_postgresql_1 \
  /bin/bash -c \
  'psql -U corla -d corla < /root/corla-test-credentials.psql'
```

Note that `coloradorla_postgresql_1` is the output of "Name" from

```sh
docker-compose ps postgresql
```

but should be consistent assuming you have checked out the code to a directory
named the same as the repository.

# Installation and Use

A document describing how to download, install, and use this system is
found in [the docs directory](docs/15_installation.md).

# System Documentation

Documentation about this project and the Colorado RLA system includes:
* a [User Manual (docx)](docs/user_manual.docx)
  with an overview of the system,
* a [County Run Book (docx)](docs/county_runbook.docx) and
  [State Run Book (docx)](docs/sos_runbook.docx) for system users,
* a [description of our development process and methodology](docs/35_methodology.md),
* a [developer document](docs/25_developer.md) that contains our
  developer instructions, including the project history, technologies
  in use, dependencies, how to build the system, how we perform
  quality assurance, how we perform validation and verification, and
  what the build status of the project is,
* the [system requirements](docs/50_requirements.md),
* the [formal system specification](docs/55_specification.md),
* the [means by which we validate and verify the system](docs/40_v_and_v.md),
* a [glossary](docs/89_glossary.md) of the domain terminology used in
  the system,
* a full [bibliography](docs/99_bibliography.md) is available.
* a [document describing how we perform project management](docs/30_project_management.md),
* the [license](LICENSE.md) under which this software is made available,
  and
* all [contributors](#contributors) to the design and development of
  this system are listed below.

# Contributors

* Joey Dodds (Principled Computer Scientist) RLA core computations
  implementation
* Joseph Kiniry (Principled CEO and Chief Scientist) Project Head,
  author of formal specification, design and implementation of ASMs
  and 2FA
* Michael Kiniry (Principled Documentarian) User-facing documentation
* Neal McBurnett (Principled Elections Auditing Expert and Computer Scientist) RLA expert,
  design and implementation of data export application
  and automatic server test infrastructure
* Morgan Miller (Principled Usability Specialist) UX expert, conducted
  interviews with CDOS and County personnel, initial UI design
* Joe Ranweiler (Principled Computer Scientist) Principal author of
  RLA Tool Client
* Stephanie Singer (Principled Elections Expert and Data Scientist) Query design for
  data export application, user-facing documentation
* Daniel Zimmerman (Principled Computer Scientist) Principal author of
  RLA Tool Server
* Mike Prasad (CDOS Developer/Architect) Authored enhancements to RLA Tool Client and Server

More information about our team members [is available](docs/70_team.md).
