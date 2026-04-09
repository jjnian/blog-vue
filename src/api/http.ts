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

async function request<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const response = await fetch(resolveUrl(input), {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
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
