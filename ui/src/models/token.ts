
export interface TokenWrapper {
  token: string
}

/** a JWT created by the auth-service. */
export interface AuthServiceToken {
  user: {
    username: string
    email: string
  }
  exp: number
}
/** a JWT created by webmodelica's backend. */
export interface WebmodelicaToken {
  username: string
  exp: number
  iss: number
}
