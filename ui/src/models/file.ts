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

function fileName(f:File): string {
  return f.relativePath.substring(f.relativePath.lastIndexOf('/')+1, f.relativePath.length)
}

export function setId(root:FileNode): FileNode {
  return {
    id: root.path,
    children: (root.children) ? root.children.map(setId) : undefined,
    ...root
  }
}

export function renameFile(root:FileNode, oldFile:File, newFile:File): FileNode {
  if(root.file && root.file === oldFile)
    return { ...root, path: fileName(newFile), file: newFile }
  else if(root.children) {
    const childs: FileNode[] = R.map(n => renameFile(n, oldFile, newFile), root.children)
    return {
      ...root,
      children: childs
    }
  } else {
    return root
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
