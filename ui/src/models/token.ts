
export interface TokenWrapper {
  token: string
}

/** a JWT created by the auth-service. */
export interface AuthServiceToken {
  data: {
    username: string
    email: string
    first_name?: string
    last_name?: string
  }
  exp: number
}
/** a JWT created by webmodelica's backend. */
export interface WebmodelicaToken {
  username: string
  exp: number
  iss: number
}
/** the authentication token. */
export interface JwtToken {
  username: string
  expires: Date
  raw: string
}
