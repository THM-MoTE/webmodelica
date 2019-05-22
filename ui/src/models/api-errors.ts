
export class ApiError {
  constructor(
    readonly code: number,
    readonly statusText: string) {

  }

  isClientError():boolean {
    return this.code >= 400 && this.code < 500;
  }
  isNotFound():boolean {
    return this.code == 404;
  }
  isBadRequest():boolean {
    return this.code == 400;
  }
  isSevere():boolean {
    return this.code >= 500;
  }
}
