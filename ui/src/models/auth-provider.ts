import * as R from 'ramda'

export interface AuthProvider {
  key: string
  color?: string
  name: string
  uri: string
  icon?: string
}

const iconMap = {
  github: 'mark-github',
  developer: 'database',
  cas: 'mortar-board'
}

function findIcon(key:string, provider:any): string | undefined {
  if(provider.icon) return provider.icon
  else {
    return R.prop(key)(iconMap)
  }
}

export function parseAuthPayload(baseUri:string, obj:any): AuthProvider[] {
  return R.keys(obj.providers).map(k => {
    const provider = obj.providers[k]
    return ({ key: k, uri: baseUri+`/${String(k)}`, icon: findIcon(String(k), provider), ...provider })
  })
}
