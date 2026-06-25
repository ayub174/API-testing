import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import BooksPage from './BooksPage';
import type { Permission } from '../types';

// Mocka api-klienten så testet inte gör nätverksanrop.
vi.mock('../api/client', () => ({
  api: {
    get: vi.fn().mockResolvedValue({
      count: 1,
      books: [{ id: 1, title: 'Testbok', author: 'A', price: 10 }],
    }),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
  },
  ApiError: class ApiError extends Error {
    status = 0;
  },
}));

// Mocka auth-kontexten så vi kan styra behörigheterna.
let permissions: Permission[] = [];
vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    hasPermission: (p: Permission) => permissions.includes(p),
  }),
}));

describe('BooksPage behörighetsstyrd UI', () => {
  beforeEach(() => {
    permissions = [];
  });

  it('visar Ta bort-knapp när BOOK_DELETE finns', async () => {
    permissions = ['BOOK_DELETE'];
    render(<BooksPage />);
    await waitFor(() => expect(screen.getByTestId('books-table')).toBeInTheDocument());
    expect(screen.getByTestId('delete-1')).toBeInTheDocument();
  });

  it('döljer Ta bort-knapp utan BOOK_DELETE', async () => {
    permissions = ['BOOK_READ'];
    render(<BooksPage />);
    await waitFor(() => expect(screen.getByTestId('books-table')).toBeInTheDocument());
    expect(screen.queryByTestId('delete-1')).not.toBeInTheDocument();
  });
});
