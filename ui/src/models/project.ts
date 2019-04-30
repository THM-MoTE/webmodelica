

export interface Project {
  id: string,
  name: string,
  owner: string,
  visibility: string
}

const publicVisibility = "public"
const privateVisibility = "private"

export function projectIsPrivate(p:Project): boolean {
  return p.visibility === privateVisibility
}
export function projectIsPublic(p: Project): boolean {
  return p.visibility === publicVisibility
}
