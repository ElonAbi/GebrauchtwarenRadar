module.exports = {
  parser: "@typescript-eslint/parser",
  parserOptions: {
    project: true,
    tsconfigRootDir: __dirname
  },
  plugins: ["@typescript-eslint"],
  extends: ["eslint:recommended", "plugin:@typescript-eslint/recommended", "plugin:@typescript-eslint/recommended-type-checked", "plugin:@typescript-eslint/strict", "prettier"],
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  ignorePatterns: ["dist", "node_modules"],
  rules: {
    "@typescript-eslint/consistent-type-imports": "warn"
  }
};
