#code from https://gist.github.com/cdown/1163649
urlencode() {
    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C
    
    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done
    
    LC_COLLATE=$old_lc_collate
}

API_username=$(urlencode "${API_username}")
API_password=$(urlencode "${API_password}")

echo https://${API_username}:${API_password}@* > WSDL_login.txt