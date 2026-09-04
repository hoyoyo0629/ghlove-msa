// 각 서비스 접두사는 vite.config.js의 dev 프록시(및 운영 Kong 라우트)의 strip_path
// 규칙과 1:1로 대응한다 - 여기서 바뀌면 저기도 같이 바뀌어야 한다.
const SERVICE_PREFIX = {
  member: '/member',
  donation: '/donation',
  point: '/point',
  gift: '/gift',
  order: '/order',
  admin: '/admin',
}

async function parseErrorMessage(res) {
  let message = `요청을 처리하지 못했습니다. (${res.status})`
  try {
    const data = await res.json()
    if (data?.message) message = data.message
  } catch {
    // 에러 응답이 JSON이 아닐 수도 있음 - 기본 메시지를 그대로 쓴다.
  }
  return message
}

async function request(service, path, { method = 'GET', body } = {}) {
  const res = await fetch(SERVICE_PREFIX[service] + path, {
    method,
    credentials: 'include',
    headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (!res.ok) throw new Error(await parseErrorMessage(res))
  if (res.status === 204) return null
  return res.json()
}

/** 파일 첨부(qna 등)처럼 multipart/form-data로 보내야 하는 요청 전용 - Content-Type을
 * 직접 지정하지 않아야 브라우저가 경계 문자열(boundary)을 자동으로 채운다. */
async function requestMultipart(service, path, formData, method = 'POST') {
  const res = await fetch(SERVICE_PREFIX[service] + path, { method, credentials: 'include', body: formData })
  if (!res.ok) throw new Error(await parseErrorMessage(res))
  if (res.status === 204) return null
  return res.json()
}

/** InterestLocgovController#add()처럼 member Thymeleaf 화면의 기존 @RequestParam 계약을
 * 그대로 공유하는 소수의 엔드포인트 전용 - 일반 api.post()의 JSON 바디로는 @RequestParam이
 * 바인딩되지 않는다(둘 다 같은 백엔드 계약을 쓰므로 이 요청만 form-urlencoded로 보낸다). */
async function requestForm(service, path, params) {
  const res = await fetch(SERVICE_PREFIX[service] + path, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams(params).toString(),
  })
  if (!res.ok) throw new Error(await parseErrorMessage(res))
  if (res.status === 204) return null
  return res.json()
}

export const api = {
  get: (service, path) => request(service, path),
  post: (service, path, body) => request(service, path, { method: 'POST', body: body ?? {} }),
  put: (service, path, body) => request(service, path, { method: 'PUT', body: body ?? {} }),
  delete: (service, path) => request(service, path, { method: 'DELETE' }),
  postForm: (service, path, formData) => requestMultipart(service, path, formData),
  postUrlEncoded: (service, path, params) => requestForm(service, path, params),
  assetUrl: (service, path) => SERVICE_PREFIX[service] + path,
}
