# Stellar Federation Address Service
This projects aims to implement the level-2 bounty [federation-server](https://github.com/tyvdh/stellar-quest-bounties/blob/main/bounties/level-2/federation-server.md) from the [Stellar Quest Bounty Programm](https://github.com/tyvdh/stellar-quest-bounties). 
It is used for linking an existing social-media-account provider like Discord with your Stellar Public Key, to enable the use of an easy to remember federated address linked to your social-media account e.g. `DieKautz#3846*example.name`.

# Usage
An image of the latest release is deployed at [ssfs.diekautz.dev](https://ssfs.diekautz.dev) for testing purposes where you can find a simple UI to manage and delete your federated address for your social-media-account. Please note that currently just Discord is implemented for authentication.

## SEP-0002
The federetion end of the service currently implements a fully functional resolver (request type `name`) in accordance with [the spec](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0002.md). You can query it at: `https://ssfs.diekautz.dev/federation`

# Deployment
If you want to test out the service on you own infrastructure you can
 - download the [latest release](https://github.com/DieKautz/stellar-federation-server/releases/latest) and run the application using Java
 - or compile from source using Gradle.

In addition to that you have to configure two things:

 1. customize the application.yml with your domain and developer credentials and put it in the same folder as the jar.
 2. run a MySQL server on the same host with this default configuration
    - port: `3306`
    - username: `fed_admin`
    - password: `secret`
    - database: `fed_address`
    - _Note that this can be changed by adjusting your `application.yml`_

# License
This project can be built upon under the conditions of the [MIT License](https://github.com/DieKautz/stellar-federation-server/blob/master/LICENSE).
