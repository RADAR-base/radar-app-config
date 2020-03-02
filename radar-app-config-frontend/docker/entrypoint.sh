#!/bin/sh

ROOT_DIR=/usr/share/nginx/html

# Replace env vars in JavaScript files
echo "Replacing env vars in JS"
for file in $ROOT_DIR/*.js* $ROOT_DIR/index.html;
do
  echo "Processing $file ...";

#  sed -i 's|/\?VUE_APP_BASE_URL|'${VUE_APP_BASE_URL}'|g' $file
  sed -i 's|RACF_BASE_URL|'${RACF_BASE_URL}'|g' $file
  sed -i 's|RACF_AUTH_API|'${RACF_AUTH_API}'|g' $file
  sed -i 's|RACF_AUTH_CALLBACK|'${RACF_AUTH_CALLBACK}'|g' $file
  sed -i 's|RACF_BACKEND_URL|'${RACF_BACKEND_URL}'|g' $file

#  sed -i 's|VUE_APP_CLIENT_ID|'${VUE_APP_CLIENT_ID}'|g' $file
done

echo "Static files ready"
exec "$@"
