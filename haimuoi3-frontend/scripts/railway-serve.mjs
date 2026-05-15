/**
 * Production static server for Railway.
 * Binds 0.0.0.0 and process.env.PORT (required by Railway edge routing).
 */
import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const distPath = path.join(__dirname, '../dist/haimuoi3-frontend/browser');
const port = Number(process.env.PORT);
const host = '0.0.0.0';

if (!port || Number.isNaN(port)) {
  console.error('[railway-serve] PORT env is required');
  process.exit(1);
}

const app = express();
app.use(express.static(distPath, { index: ['index.html', 'index.csr.html'] }));

// SPA fallback for client-side routes not prerendered
app.get(/^(?!.*\.\w+$).*/, (_req, res) => {
  res.sendFile(path.join(distPath, 'index.csr.html'), (err) => {
    if (err) {
      res.sendFile(path.join(distPath, 'index.html'));
    }
  });
});

app.listen(port, host, () => {
  console.log(`[railway-serve] Listening on http://${host}:${port} (dist=${distPath})`);
});
