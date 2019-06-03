/** modelica tokenizer for the monaco editor.
 * see: https://microsoft.github.io/monaco-editor/monarch.html
*/
let ModelicaTokenizer = {
  keywords: [
    "algorithm", "and", "annotation", "assert", "block", "break", "class", "connect", "connector", "constant", "constrainedby", "der", "discrete", "each",
    "else", "elseif", "elsewhen", "encapsulated", "end", "enumeration", "equation", "expandable", "extends", "external", "false", "final", "flow", "for",
    "function", "if", "import", "impure", "in", "initial", "inner", "input", "loop", "model", "not", "operator", "or", "outer", "output", "package", "parameter",
    "partial", "protected", "public", "pure", "record", "redeclare", "replaceable", "return", "stream", "then", "true", "type", "when", "while", "within"
  ],
  typeKeywords: [
    'Real', 'Boolean', 'Integer', 'String'
  ],

  operators: [
    '=', '>', '<', '!', '~', '?', ':', '==', '<=', '>=', '!=',
    '&&', '||', '++', '--', '+', '-', '*', '/', '&', '|', '^', '%',
    '<<', '>>', '>>>', '+=', '-=', '*=', '/=', '&=', '|=', '^=',
    '%=', '<<=', '>>=', '>>>='
  ],

  // we include these common regular expressions
  symbols: /[=><!~?:&|+\-*\/\^%]+/,

  // C# style strings
  escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

  tokenizer: {
    root: [
      [/[a-z_$][\w\.$]*/, {
        cases: {
          '@keywords': { token: 'keyword.$0' },
          '@default': 'identifier'
        }
      }],
      // whitespace
      { include: '@whitespace' },

      // delimiters and operators
      [/[{}()\[\]]/, '@brackets'],
      // numbers
      [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
      [/0[xX][0-9a-fA-F]+/, 'number.hex'],
      [/\d+/, 'number'],

      // strings
      [/"([^"\\]|\\.)*$/, 'string.invalid'],  // non-teminated string
      [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],

      // characters
      [/'[^\\']'/, 'string'],
      [/(')(@escapes)(')/, ['string', 'string.escape', 'string']],
      [/'/, 'string.invalid']

    ],
    comment: [
      [/[^\/*]+/, 'comment'],
      [/\/\*/, 'comment', '@push'],    // nested comment
      ["\\*/", 'comment', '@pop'],
      [/[\/*]/, 'comment']
    ],
    string: [
      [/[^\\"]+/, 'string'],
      [/@escapes/, 'string.escape'],
      [/\\./, 'string.escape.invalid'],
      [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
    ],
    whitespace: [
      [/[ \t\r\n]+/, 'white'],
      [/\/\*/, 'comment', '@comment'],
      [/\/\/.*$/, 'comment'],
    ],
  }
}

export function languageName(): string { return 'modelica' }
export function tokenizer(): any { return ModelicaTokenizer }
