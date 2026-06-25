export type Permission =
  | 'BOOK_READ'
  | 'BOOK_CREATE'
  | 'BOOK_UPDATE'
  | 'BOOK_DELETE'
  | 'LOAN_BORROW'
  | 'LOAN_RETURN'
  | 'LOAN_VIEW_OWN'
  | 'LOAN_VIEW_ALL'
  | 'LOAN_MANAGE'
  | 'USER_READ'
  | 'USER_MANAGE'
  | 'ACCOUNT_MANAGE'
  | 'PERMISSION_MANAGE';

export type Role = 'ADMIN' | 'HANDLAGGARE' | 'ANVANDARE';

export interface LoginResponse {
  token: string;
  username: string;
  role: Role;
  permissions: Permission[];
  expiresIn: number;
}

export interface Book {
  id: number;
  title: string;
  author: string;
  price: number;
  stock?: number;
  genre?: string;
  available?: boolean;
  createdAt?: string;
}

export interface BooksResponse {
  count: number;
  books: Book[];
}

export interface User {
  id: number;
  name: string;
  email: string;
  role?: string;
}

export interface Account {
  id: number;
  username: string;
  role: Role;
  permissions: Permission[];
}

export interface Loan {
  id: number;
  bookId: number;
  bookTitle: string;
  username: string;
  borrowedAt: string;
  dueDate: string;
  returnedAt: string | null;
  returned: boolean;
  overdue: boolean;
}
