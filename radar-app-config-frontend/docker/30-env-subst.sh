#!/bin/sh

set -e

function replace() {
  local find="$1" replace="$2" file="$3" tmpfile="$(mktemp)"
  sed -r "s|${find}|${replace}|g" "$file" > "$tmpfile"
  cat "$tmpfile" > "$file"
  rm "$tmpfile"
}

cd /usr/share/nginx/html

BASE_HREF=$(echo -n "${BASE_HREF}" | sed 's|/$||' )

case "$BASE_HREF" in
  /*)
    # Do nothing
    ;;
  *)
    BASE_HREF="/$BASE_HREF"
    ;;
esac

# replace BASE_HREF value
replace "(\<base\ href\=\")([^\"]+)" "\\1${BASE_HREF}/" index.html
replace "BASE_HREF" "$BASE_HREF" /etc/nginx/conf.d/default.conf

for main in main*.js; do
  replace "RACF_BASE_URL" "${BASE_HREF}" "$main"
  replace "RACF_AUTH_API" "${AUTH_URL}" "$main"
  replace "RACF_AUTH_CALLBACK" "${AUTH_CALLBACK_URL}" "$main"
  replace "RACF_BACKEND_URL" "${APP_CONFIG_URL}" "$main"
done

for f in main*.js index.html; do
  gzip -c $f > $f.gz
done
