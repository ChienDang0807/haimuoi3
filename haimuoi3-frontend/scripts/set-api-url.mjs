import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const targetPath = path.join(__dirname, '../src/environments/environment.prod.ts');

const raw = process.env.API_URL?.trim();
if (!raw) {
  console.error(
    '[set-api-url] Missing API_URL. Example: https://your-backend.up.railway.app/api'
  );
  process.exit(1);
}

let apiUrl = raw.replace(/\/+$/, '');
if (!/^https?:\/\//i.test(apiUrl)) {
  apiUrl = `https://${apiUrl}`;
}
if (!apiUrl.endsWith('/api')) {
  apiUrl += '/api';
}

const content = `export const environment = {
  production: true,
  apiUrl: '${apiUrl}'
};
`;

fs.writeFileSync(targetPath, content, 'utf8');
console.log(`[set-api-url] Wrote ${targetPath} with apiUrl=${apiUrl}`);
