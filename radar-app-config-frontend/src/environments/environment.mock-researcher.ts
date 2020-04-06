export const environment = {
  envName: 'mock-researcher',
  production: false,
  baseURL: 'http://localhost:4200/',
  authAPI: 'http://localhost:4200/managementportal/oauth',
  clientId: 'appconfig_frontend',
  authCallback: 'http://localhost:4200/login',
  backendUrl: '/api',
  fakeCode: {code: 12346},
  fakeJwtToken: {
    // tslint:disable-next-line:max-line-length
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJyZXNfYXBwY29uZmlnIiwic3ViIjoicGV5bWFuIiwic291cmNlcyI6W10sImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInVzZXJfbmFtZSI6IkpvaG4iLCJyb2xlcyI6W10sInNjb3BlIjpbIlNVQkpFQ1QuVVBEQVRFIiwiUFJPSkVDVC5DUkVBVEUiLCJQUk9KRUNULlVQREFURSIsIk1FQVNVUkVNRU5ULkNSRUFURSIsIlBST0pFQ1QuUkVBRCIsIk9BVVRIQ0xJRU5UUy5SRUFEIiwiU1VCSkVDVC5SRUFEIl0sImlzcyI6Ik1hbmFnZW1lbnRQb3J0YWwiLCJleHAiOjE1ODAyOTczNjEsImlhdCI6MTU4MDI5Mzc2MSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9SRVNFQVJDSEVSIl0sImNsaWVudF9pZCI6ImFwcGNvbmZpZ19mcm9udGVuZCJ9.IlQxCMKxR8JCy9r6xG7DV-KSTjcsBY0Y09e8xE9SFbs'
  }
};
