import {JwtToken} from './index'

export interface UserAuth {
  username: string
  displayName: string
  token: JwtToken
}
