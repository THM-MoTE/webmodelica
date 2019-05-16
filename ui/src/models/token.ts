
export interface TokenWrapper {
  token: string
}

export interface AuthServiceToken {
  user: {
    username: string
    email: string
  }
  exp: number
}
export interface WebmodelicaToken {
  username: string
  exp: number
  iss: number
}
