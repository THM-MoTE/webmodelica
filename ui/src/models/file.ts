import * as R from 'ramda'

export interface File {
  relativePath: string,
  content: string
}

export interface FilePath {
  relativePath: string
}

export interface FileNode {
  path: string,
  id?: string,
  children?: FileNode[],
  file?: FilePath,
  toggled?: boolean
}

function fileName(f:File|FilePath): string {
  return f.relativePath.substring(f.relativePath.lastIndexOf('/')+1, f.relativePath.length)
}

export function exists(root:FileNode, name:string): boolean {
  return (root.file &&
    root.file.relativePath === name) ||
    (root.children && R.any(n => exists(n, name), root.children)) ||
    false
}

export function setId(root:FileNode): FileNode {
  return {
    id: root.path,
    children: (root.children) ? root.children.map(setId) : undefined,
    ...root
  }
}
export function toggleRoot(root:FileNode): FileNode {
  return { ...root, toggled: true }
}

export function renameFile(root: FileNode, oldFile: FilePath, newFile: FilePath): FileNode {
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

export function removeFile(root:FileNode, file:FilePath): FileNode | undefined {
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
