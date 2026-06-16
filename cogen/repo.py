import requests
import re as _re
import base64

def fetch_repo(url):
    # Check memory first
    try:
        from memory import save_repo, get_repo
        cached = get_repo(url)
        if cached:
            result = f"REPO: {cached['owner']}/{cached['repo_name']} (from memory)\n"
            result += f"Description: {cached.get('summary', 'None')}\n"
            result += f"FILES:\n" + "\n".join(cached.get('files', [])[:50])
            result += f"\n\nREADME:\n{cached.get('readme', '')[:2000]}"
            return result
    except:
        pass
    # Original fetch below
    match = _re.search(r'github\.com/([\w-]+)/([\w-]+)', url)
    if not match:
        return None
    owner = match.group(1)
    repo = match.group(2)
    try:
        api_url = f"https://api.github.com/repos/{owner}/{repo}"
        resp = requests.get(api_url, timeout=10)
        if resp.status_code != 200:
            return None
        info = resp.json()

        readme_url = f"https://api.github.com/repos/{owner}/{repo}/readme"
        readme_resp = requests.get(readme_url, timeout=10)
        readme_text = ""
        if readme_resp.status_code == 200:
            readme_data = readme_resp.json()
            readme_text = base64.b64decode(readme_data["content"]).decode("utf-8")[:2000]

        tree_url = f"https://api.github.com/repos/{owner}/{repo}/git/trees/main?recursive=1"
        tree_resp = requests.get(tree_url, timeout=10)
        files = []
        if tree_resp.status_code == 200:
            for item in tree_resp.json().get("tree", []):
                if item["type"] == "blob":
                    files.append(item["path"])

        result = f"REPO: {owner}/{repo}\n"
        result += f"Description: {info.get('description', 'None')}\n"
        result += f"Stars: {info.get('stargazers_count', 0)}\n"
        result += f"Language: {info.get('language', 'Unknown')}\n\n"
        result += f"FILES ({len(files)} total):\n" + "\n".join(files[:50]) + "\n\n"
        result += f"README:\n{readme_text}"
        # Save to persistent memory
        try:
            from memory import save_repo
            import re
            m = _re.search(r'github\.com/([\w-]+)/([\w-]+)', url)
            if m:
                clean_files = [
                    f for f in files
                    if not any(skip in f for skip in
                    ['artifacts/', 'dist/', 'node_modules/', '.replit', 'pino'])
                ]
                save_repo(url, m.group(1), m.group(2),
                         info.get('description', ''),
                         clean_files[:50], readme_text)
        except:
            pass
        return result[:15000]
    except Exception:
        return None

def fetch_file(repo_url, filepath):
    match = _re.search(r'github\.com/([\w-]+)/([\w-]+)', repo_url)
    if not match:
        return None
    owner = match.group(1)
    repo = match.group(2)
    try:
        url = f"https://api.github.com/repos/{owner}/{repo}/contents/{filepath}"
        resp = requests.get(url, timeout=10)
        if resp.status_code == 200:
            data = resp.json()
            if data.get("encoding") == "base64":
                return base64.b64decode(data["content"]).decode("utf-8")[:10000]
        return None
    except Exception:
        return None

def is_github_url(text):
    return bool(_re.search(r'github\.com/[\w-]+/[\w-]+', text))

def extract_github_url(text):
    match = _re.search(r'https?://github\.com/[\w-]+/[\w-]+', text)
    if match:
        return match.group()
    match = _re.search(r'github\.com/[\w-]+/[\w-]+', text)
    if match:
        return "https://" + match.group()
    return None

def extract_filepath(text):
    """Try to detect if user is asking about a specific file"""
    patterns = [
        r'show me ([\w./]+\.[\w]+)',
        r'read ([\w./]+\.[\w]+)',
        r'open ([\w./]+\.[\w]+)',
        r'what is in ([\w./]+\.[\w]+)',
        r'contents of ([\w./]+\.[\w]+)',
        r'show ([\w./]+\.[\w]+) in',
        r'file ([\w./]+\.[\w]+)',
    ]
    for pattern in patterns:
        match = _re.search(pattern, text.lower())
        if match:
            return match.group(1)
    return None
