import * as R from 'ramda'

export interface File {
  relativePath: string,
  content: string
}

export interface FileNode {
  path: string,
  id?: string,
  children?: FileNode[],
  file?: File,
  toggled?: boolean
}

export function setId(root:FileNode): FileNode {
  return {
    id: root.path,
    children: (root.children) ? root.children.map(setId) : undefined,
    ...root
  }
}


export function removeFile(root:FileNode, file:File): FileNode | undefined {
  if (root.file && root.file.relativePath === file.relativePath) { return undefined }
  else if(root.children) {
    const childs: FileNode[] = R.map(n => removeFile(n, file), root.children).filter((n): n is FileNode => n !== undefined)
    return {
      ...root,
      children: childs
    }
  } else {
    return root
  }
}
