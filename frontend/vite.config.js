import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 80,
    strictPort: true,
    host: "0.0.0.0",
    proxy: {
      "/api": {
        target: "http://backend:8080",
        changeOrigin: true,
      },
      "/auth": {
        target: "http://backend:8080",
        changeOrigin: true,
        configure: (proxyServer) => {
          proxyServer.on("proxyReq", (proxyRequest) => {
          });
        },
      },
    },
  },
});
