export const authConfig = {
  clientId: 'oauth2-pkce-client',
  authorizationEndpoint: 'http://localhost:8181/realms/fitness-project/protocol/openid-connect/auth',
  tokenEndpoint: 'http://localhost:8181/realms/fitness-project/protocol/openid-connect/token',
  redirectUri: `${window.location.origin}/`,
  scope: 'openid profile email offline_access',
  onRefreshTokenExpire: (event)=> event.logIn(),
}