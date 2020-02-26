export const environment = {
  envName: 'mock',
  production: false,

  authAPI: 'http://localhost:4200/appconfig/managementportal/oauth',
  clientId: 'appconfig_frontend',
  authCallback: 'http://localhost:4200/appconfig/login',

  fakeCode: {code: 84936},
  fakeJwtToken: {
    // tslint:disable-next-line:max-line-length
    token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJyZXNfYXBwY29uZmlnIiwic3ViIjoicGV5bWFuIiwic291cmNlcyI6W10sImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInVzZXJfbmFtZSI6InBleW1hbiIsInJvbGVzIjpbXSwic2NvcGUiOlsiU1VCSkVDVC5VUERBVEUiLCJQUk9KRUNULkNSRUFURSIsIlBST0pFQ1QuVVBEQVRFIiwiTUVBU1VSRU1FTlQuQ1JFQVRFIiwiUFJPSkVDVC5SRUFEIiwiT0FVVEhDTElFTlRTLlJFQUQiLCJTVUJKRUNULlJFQUQiXSwiaXNzIjoiTWFuYWdlbWVudFBvcnRhbCIsImV4cCI6MTU4MDI5NzM2MSwiaWF0IjoxNTgwMjkzNzYxLCJhdXRob3JpdGllcyI6WyJST0xFX1NZU19BRE1JTiJdLCJjbGllbnRfaWQiOiJhcHBjb25maWdfZnJvbnRlbmQifQ.Hf2kzUm0B1ciMeDmIuNDYVapg72pPtXSGdSwx-Ubw9aOBGIV7Zw7WE1TPLdpwDCD-2K1yO1iBbjgb_o6xDiiAg',
  },
};
