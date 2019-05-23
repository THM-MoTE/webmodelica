export interface File {
  relativePath: string,
  content: string
}

export interface FileNode {
  name: string,
  children?: FileNode[],
  file?: File,
  toggled?: boolean
}
