export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

const API_PREFIX = '/api';

function resolveUrl(input: RequestInfo | URL): RequestInfo | URL {
  if (typeof input !== 'string') {
    return input;
  }

  if (/^https?:\/\//.test(input) || input.startsWith(API_PREFIX + '/')) {
    return input;
  }

  if (input.startsWith('/')) {
    return `${API_PREFIX}${input}`;
  }

  return `${API_PREFIX}/${input}`;
}

function getAuthHeaders(): Record<string, string> {
  const token = localStorage.getItem('token');
  if (token) {
    return { Authorization: `Bearer ${token}` };
  }
  return {};
}

async function request<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const response = await fetch(resolveUrl(input), {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    if (response.status === 401) {
      // Clear auth and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      throw new Error('未授权，请重新登录');
    }
    throw new Error(`HTTP ${response.status}`);
  }

  const result = (await response.json()) as ApiResult<T>;
  if (result.code !== 200) {
    throw new Error(result.message || 'Request failed');
  }

  return result.data;
}

export const apiGet = <T>(url: string): Promise<T> => request<T>(url);

export const apiPost = <T, B = unknown>(url: string, body: B): Promise<T> =>
  request<T>(url, {
    method: 'POST',
    body: JSON.stringify(body),
  });

export const apiPut = <T, B = unknown>(url: string, body?: B): Promise<T> =>
  request<T>(url, {
    method: 'PUT',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

export const apiDelete = <T>(url: string): Promise<T> =>
  request<T>(url, { method: 'DELETE' });

export const apiPutParams = <T>(url: string, params: Record<string, string | number | boolean>): Promise<T> => {
  const query = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    query.set(k, String(v));
  }
  return request<T>(`${url}?${query.toString()}`, { method: 'PUT' });
};
