// Difficulty: "Easy"
// Language definition for Java
let ModelicaTokenizer = {
  tokenizer: {
    root: [
      [/algorithm/, "keyword"],
      [/model/, "keyword"],
      [/function/, "keyword"],
      [/end/, "keyword"],
    ]
  }
}

export function languageName(): string { return 'modelica' }
export function tokenizer(): any { return ModelicaTokenizer }
