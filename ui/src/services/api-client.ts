
export class ApiClient {

  private base: string
  private token?: string

  constructor(baseUri:string) {
    this.base = baseUri
    this.token = undefined
  }

  private userUri():string {
    return this.base+"users/"
  }

  public login(user:string, pw:string): Promise<any> {
    return fetch(this.userUri()+"login", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({username: user, password: pw})
    })
  }
}
