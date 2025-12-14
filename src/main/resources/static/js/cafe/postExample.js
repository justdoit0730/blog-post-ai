const editor = new toastui.Editor({
  el: document.querySelector('#editor'),
  height: '500px',
  initialEditType: 'wysiwyg',
  previewStyle: 'vertical'
});

async function cacheReplaceAndRender() {
    const contentsBody = document.getElementById('contentsBody');
    const contentsClearBtnBody = document.getElementById('contentsClear');

    let html = editor.getHTML();
    console.log(editor.getHTML())

    // Base64 이미지 추출
    const imgElements = [...html.matchAll(/<img[^>]+src=["'](data:image\/[^"']+)["']/g)]
                        .map(m => m[1]);

    if (imgElements.length > 0) {

        // FormData 생성
        const formData = new FormData();

        // ★ forEach → for...of (await 가능)
        for (let i = 0; i < imgElements.length; i++) {
            const base64 = imgElements[i];

            const res = await fetch(base64);
            const blob = await res.blob();

            formData.append("images", blob, `image${i}.png`);
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        // 서버로 전송
        const response = await fetch('/cafe/uploadCacheImages', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        });

        const urls = await response.json();

        // Base64 → S3 URL 치환
        let replacedHtml = html;
        for (let i = 0; i < imgElements.length; i++) {
            replacedHtml = replacedHtml.replace(imgElements[i], urls[i]);
        }

        html = replacedHtml;
    }

    // 에디터 레이아웃 유지하면서 HTML 렌더링
    let simplifiedHtml = preserveToastEditorLayout(html);
    contentsBody.innerHTML = simplifiedHtml;

    // 초기화 버튼
    contentsClearBtnBody.innerHTML = '';
    const clearBtn = document.createElement('button');
    clearBtn.type = 'button';
    clearBtn.id = 'writeClear';
    clearBtn.className = 'btnset btnset-line-dark';
    clearBtn.textContent = '초기화';
    clearBtn.addEventListener('click', () => {
        contentsBody.innerHTML = '';
        contentsClearBtnBody.innerHTML = '';
    });
    contentsClearBtnBody.appendChild(clearBtn);
}

function preserveToastEditorLayout(html) {
  if (!html) return "";
  return html
    .replace(/<p><br><\/p>/gi, '<br>')
    .replace(/<\/p>\s*<p>/gi, '<br>')
    .replace(/<\/?p[^>]*>/gi, '')
    .replace(/(<br\s*\/?>\s*){2,}/gi, match => '<br>'.repeat(match.match(/<br/gi).length))
    .trim();
}

document.getElementById('postTestBtn').addEventListener('click', cacheReplaceAndRender);