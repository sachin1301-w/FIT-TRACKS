import React from 'react';
import ReactDOM from 'react-dom/client';

import { Provider } from 'react-redux';
import {store} from './store/store';
import { AuthProvider } from 'react-oauth2-code-pkce'
import { authConfig } from './authConfig';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider authConfig={authConfig}>
    <Provider store={store}>
      <App />
    </Provider>
    </AuthProvider>
  </React.StrictMode>
);