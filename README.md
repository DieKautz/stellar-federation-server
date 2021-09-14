[![Website](https://img.shields.io/website?down_color=grey&down_message=offline&label=test%20deployment&style=flat&up_color=brightgreen&up_message=online&url=https%3A%2F%2Fssfs.diekautz.dev)](https://ssfs.diekautz.dev)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/DieKautz/stellar-federation-server?label=latest%20release)](https://github.com/DieKautz/stellar-federation-server/releases/latest)
![GitHub last commit](https://img.shields.io/github/last-commit/DieKautz/stellar-federation-server)
[![GitHub](https://img.shields.io/github/license/DieKautz/stellar-federation-server?color=brightgreen)](/LICENSE)

# Stellar Federation Address Service
This projects aims to implement the level-2 bounty [federation-server](https://github.com/tyvdh/stellar-quest-bounties/blob/main/bounties/level-2/federation-server.md) from the [Stellar Quest Bounty Programm](https://github.com/tyvdh/stellar-quest-bounties). 
It is used for linking an existing social-media-account provider like Discord with your Stellar Public Key, to enable the use of an easy to remember federated address linked to your social-media account e.g. `DieKautz#3846*dc.example.name`.

# Usage
An image of the latest release is deployed at [ssfs.diekautz.dev](https://ssfs.diekautz.dev) for testing purposes where you can find a simple UI to manage and delete your federated address for your social-media-account. Please note that currently just Discord and GitHub is implemented for authentication.

## SEP-0002
The federetion end of the service currently implements a fully functional resolver (supporting queries of type `name` and `id`) in accordance with [the spec](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0002.md). You can query it at: `https://ssfs.diekautz.dev/federation`

# Deployment
If you want to test out the service on you own infrastructure you can
 - download the [latest release](https://github.com/DieKautz/stellar-federation-server/releases/latest) and run the application using Java
 - or compile from source using Gradle.

In addition to that you have to configure two things:

 1. customize the `application.yml` with your domain and developer credentials and put it in the same folder as the jar.
 2. run a MySQL server on the same host with this default configuration
    - port: `3306`
    - username: `fed_admin`
    - password: `secret`
    - database: `fed_address`
    - _Note that this can be changed by adjusting your `application.yml`_

## application.yml
Providing the application with an `application.yml` in the root directory, will let you customize all sorts of attributes.

Namely:
 - OAuth2 Client id/secret/callbackUrl
   - Discord  `oauth2.discord.client.(id|secret|callbackUrl)`
   - GitHub  `oauth2.github.client.(id|secret|callbackUrl)`
 - stellar federation domain (`stellar.federation.domain`)
 - optional:
   - global fields for stellar toml under /.well-known/stellar.toml (`stellar.toml.general`)
   - server http port (`server.port`)
   - database connection
     - `spring.datasource.url`
     - `spring.datasource.username`
     - `spring.datasource.password`

# License
This project can be built upon under the conditions of the [MIT License](https://github.com/DieKautz/stellar-federation-server/blob/master/LICENSE).
