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

    const base64Images = [...html.matchAll(/<img[^>]+src=["'](data:image\/[^"']+)["']/g)]
                        .map(m => m[1]);

    let replacedHtml = html;

    if (base64Images.length > 0) {
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch('/cafe/uploadCacheImages', {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ base64Images })
            });

        const urls = await response.json();

        base64Images.forEach((base64, i) => {
            replacedHtml = replacedHtml.replace(base64, urls[i]);
        });
    }

    let simplifiedHtml = preserveToastEditorLayout(replacedHtml);
    contentsBody.innerHTML = simplifiedHtml;

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