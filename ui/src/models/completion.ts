import * as monaco from 'monaco-editor';

export interface FilePosition {
  line:number
  column:number
}

export interface Complete {
  file: string
  position: FilePosition
  word: string
}

export interface Suggestion {
  kind: string,
  name: string,
  parameters?: string[],
  classComment?: string,
  type?: string
}

const typeMap: { [k: string]: monaco.languages.CompletionItemKind } = {
  'type': monaco.languages.CompletionItemKind.Interface,
  'variable': monaco.languages.CompletionItemKind.Variable,
  'function': monaco.languages.CompletionItemKind.Function,
  'keyword': monaco.languages.CompletionItemKind.Keyword,
  'package': monaco.languages.CompletionItemKind.Folder,
  'model': monaco.languages.CompletionItemKind.Class,
  'class': monaco.languages.CompletionItemKind.Class,
  'property': monaco.languages.CompletionItemKind.Property
}

export function toVSCodeComplete(wordAtCursor: string|undefined, s:Suggestion):monaco.languages.CompletionItem {
  const kind = typeMap[s.kind] || monaco.languages.CompletionItemKind.Class
  const text = (wordAtCursor) ? s.name.substring(wordAtCursor.length-1) : s.name;
  return {
    label: s.name,
    insertText: text,
    kind: kind,
    documentation: s.classComment,
    detail: s.type
  }
}
