import { useEffect, useState, type FormEvent } from 'react';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type { Book, BooksResponse } from '../types';

const EMPTY_FORM = { title: '', author: '', price: '', genre: '', stock: '' };

export default function BooksPage() {
  const { hasPermission } = useAuth();
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState<number | null>(null);

  const canCreate = hasPermission('BOOK_CREATE');
  const canUpdate = hasPermission('BOOK_UPDATE');
  const canDelete = hasPermission('BOOK_DELETE');

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<BooksResponse>('/books');
      setBooks(res.books);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte hämta böcker');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  function resetForm() {
    setForm(EMPTY_FORM);
    setEditingId(null);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    const payload = {
      title: form.title,
      author: form.author,
      price: Number(form.price),
      genre: form.genre || undefined,
      stock: form.stock ? Number(form.stock) : undefined,
    };
    try {
      if (editingId !== null) {
        await api.put<Book>(`/books/${editingId}`, payload);
      } else {
        await api.post<Book>('/books', payload);
      }
      resetForm();
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte spara boken');
    }
  }

  function startEdit(book: Book) {
    setEditingId(book.id);
    setForm({
      title: book.title,
      author: book.author,
      price: String(book.price),
      genre: book.genre ?? '',
      stock: book.stock != null ? String(book.stock) : '',
    });
  }

  async function handleDelete(id: number) {
    setError(null);
    try {
      await api.del(`/books/${id}`);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte ta bort boken');
    }
  }

  return (
    <div>
      <h2>Böcker</h2>
      {error && (
        <div className="error" role="alert" data-testid="books-error">
          {error}
        </div>
      )}

      {(canCreate || (canUpdate && editingId !== null)) && (
        <form className="book-form" onSubmit={handleSubmit} data-testid="book-form">
          <h3>{editingId !== null ? 'Redigera bok' : 'Lägg till bok'}</h3>
          <div className="form-row">
            <input
              placeholder="Titel"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              data-testid="book-title"
              required
            />
            <input
              placeholder="Författare"
              value={form.author}
              onChange={(e) => setForm({ ...form, author: e.target.value })}
              data-testid="book-author"
              required
            />
            <input
              placeholder="Pris"
              type="number"
              step="0.01"
              value={form.price}
              onChange={(e) => setForm({ ...form, price: e.target.value })}
              data-testid="book-price"
              required
            />
            <input
              placeholder="Genre"
              value={form.genre}
              onChange={(e) => setForm({ ...form, genre: e.target.value })}
            />
            <input
              placeholder="Lager"
              type="number"
              value={form.stock}
              onChange={(e) => setForm({ ...form, stock: e.target.value })}
            />
          </div>
          <div className="form-actions">
            <button type="submit" data-testid="book-save">
              {editingId !== null ? 'Spara' : 'Lägg till'}
            </button>
            {editingId !== null && (
              <button type="button" onClick={resetForm}>
                Avbryt
              </button>
            )}
          </div>
        </form>
      )}

      {loading ? (
        <p>Laddar…</p>
      ) : (
        <table className="data-table" data-testid="books-table">
          <thead>
            <tr>
              <th>Id</th>
              <th>Titel</th>
              <th>Författare</th>
              <th>Pris</th>
              <th>Genre</th>
              <th>Lager</th>
              <th>Åtgärder</th>
            </tr>
          </thead>
          <tbody>
            {books.map((book) => (
              <tr key={book.id} data-testid={`book-row-${book.id}`}>
                <td>{book.id}</td>
                <td>{book.title}</td>
                <td>{book.author}</td>
                <td>{book.price}</td>
                <td>{book.genre ?? '–'}</td>
                <td>{book.stock ?? '–'}</td>
                <td className="actions">
                  {canUpdate && (
                    <button onClick={() => startEdit(book)} data-testid={`edit-${book.id}`}>
                      Redigera
                    </button>
                  )}
                  {canDelete && (
                    <button
                      className="danger"
                      onClick={() => handleDelete(book.id)}
                      data-testid={`delete-${book.id}`}
                    >
                      Ta bort
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
