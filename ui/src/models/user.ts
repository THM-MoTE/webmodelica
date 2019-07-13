export interface UserAuth {
  username: string
  token: JwtToken
}

/** the authentication token. */
export interface JwtToken {
  username: string
  expires: Date
  raw: string
}
