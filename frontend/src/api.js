const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

function getAuthHeaders() {
  const token = localStorage.getItem('banking_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

async function handleResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const errorMsg = data.message || data.error || `HTTP error! status: ${response.status}`;
    throw new Error(errorMsg);
  }
  return data;
}

export const bankingApi = {
  // Auth
  async login(email, password) {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return handleResponse(res);
  },

  async register(data) {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return handleResponse(res);
  },

  // Customer
  async getProfile() {
    const res = await fetch(`${API_BASE_URL}/customer/profile`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async getCustomerAccounts() {
    const res = await fetch(`${API_BASE_URL}/customer/accounts`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async deposit(accountNumber, amount, description) {
    const res = await fetch(`${API_BASE_URL}/customer/transactions/deposit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ accountNumber, amount: parseFloat(amount), description })
    });
    return handleResponse(res);
  },

  async withdraw(accountNumber, amount, description) {
    const res = await fetch(`${API_BASE_URL}/customer/transactions/withdraw`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ accountNumber, amount: parseFloat(amount), description })
    });
    return handleResponse(res);
  },

  async transfer(fromAccountNumber, toAccountNumber, amount, description) {
    const res = await fetch(`${API_BASE_URL}/customer/transactions/transfer`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        fromAccountNumber,
        toAccountNumber,
        amount: parseFloat(amount),
        description
      })
    });
    return handleResponse(res);
  },

  async getTransactions(accountNumber, { type, search, page = 0, size = 10 } = {}) {
    const params = new URLSearchParams();
    if (type && type !== 'ALL') params.append('type', type);
    if (search) params.append('search', search);
    params.append('page', page);
    params.append('size', size);

    const res = await fetch(`${API_BASE_URL}/customer/accounts/${accountNumber}/transactions?${params.toString()}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Admin
  async getAdminDashboard() {
    const res = await fetch(`${API_BASE_URL}/admin/dashboard`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async getAdminCustomers(query = '', page = 0, size = 10) {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    params.append('page', page);
    params.append('size', size);

    const res = await fetch(`${API_BASE_URL}/admin/customers?${params.toString()}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async getAllAccounts() {
    const res = await fetch(`${API_BASE_URL}/admin/accounts`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async freezeAccount(accountNumber) {
    const res = await fetch(`${API_BASE_URL}/admin/accounts/${accountNumber}/freeze`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async unfreezeAccount(accountNumber) {
    const res = await fetch(`${API_BASE_URL}/admin/accounts/${accountNumber}/unfreeze`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async getAllTransactions({ type, search, page = 0, size = 15 } = {}) {
    const params = new URLSearchParams();
    if (type && type !== 'ALL') params.append('type', type);
    if (search) params.append('search', search);
    params.append('page', page);
    params.append('size', size);

    const res = await fetch(`${API_BASE_URL}/admin/transactions?${params.toString()}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  }
};
