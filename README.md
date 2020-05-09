# MoonMoonExiles
haha get it like conan exiles xd

## Commands

* `/exile <username> <reason>`
  * Permission Required: `exile.use`
  * Requests for `username` to be exiled for `reason`
* `/exile list`
  * Permission Required: `exile.admin`
  * Provides a list of all pending exiles
* `/exile approve <username>`
  * Permission: `exile.admin`
  * Approves the pending exile of `username`
* `/exile reject <username>`
  * Permission: `exile.admin`
  * Rejects the pending exile of `username`

## Permissions

### `exile.admin`
* Given to admins or Lord Jeremiah
* Allows for the ability to approve or reject exile requests
* /exile is made instant, no need to approve your own requests

### `exile.use`
* Used for plebians
* Allows for you to request your fellow man be exiled
* You must wait for approval from someone with `exile.admin` permission