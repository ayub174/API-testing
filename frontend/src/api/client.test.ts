import { afterEach, describe, expect, it, vi, beforeEach } from 'vitest';
import { api, ApiError, setToken, setUnauthorizedHandler } from './client';

describe('api client', () => {
  beforeEach(() => {
    localStorage.clear();
  });
  afterEach(() => {
    vi.restoreAllMocks();
    setUnauthorizedHandler(null);
  });

  it('bifogar Bearer-token när den finns', async () => {
    setToken('abc123');
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), { status: 200 }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await api.get('/books');

    const [, options] = fetchMock.mock.calls[0];
    expect(options.headers['Authorization']).toBe('Bearer abc123');
  });

  it('401 rensar token och anropar unauthorized-handler', async () => {
    setToken('expired');
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('', { status: 401 })));

    await expect(api.get('/users')).rejects.toBeInstanceOf(ApiError);
    expect(handler).toHaveBeenCalled();
    expect(localStorage.getItem('apitesting.token')).toBeNull();
  });

  it('403 ger ett behörighetsfel', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(new Response(JSON.stringify({ message: 'nope' }), { status: 403 })),
    );
    await expect(api.del('/books/1')).rejects.toMatchObject({ status: 403 });
  });
});
