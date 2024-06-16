
const haskellKeywords = [ "True", "False", "let", "in", "if", "then", "else", "case", "of", "\\\\", "->" ]

document.querySelectorAll(".colorize").forEach(e => colorize(e));
document.querySelectorAll(".haskell").forEach(e => colorize(e, haskellKeywords));

let simplified;

async function simplify() {
    document.getElementById("error").innerText = "";

    const r = await fetch("simplify", {
        method: "post",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            ast: document.getElementById("input").textContent
        })
    });
    const json = await r.json();

    if (!r.ok) {
        let error;
        if (json.detail.type)
            error = (json.detail.type === "syntax" ? "Syntaxfehler im AST: " : "") + json.detail.message;
        else error = json.message || json.name;
        document.getElementById("error").innerText = error;
        return;
    }

    simplified = json;
    updateOutput();
}

function updateOutput() {

    const mode = document.getElementById("output-mode").value;
    let str = simplified.in.formatted;

    for(const application of simplified.applications) {
        str += `\n-- ${application.rule} --\n`;
        if(mode === "separate") {
            str += `${application.diff.prefix}´-${application.diff.old}´${application.diff.suffix}\n-- ... gets transformed to ...\n`;
        }
        str += application.diff.prefix;
        if(mode === "inline")
            str += `´-${application.diff.old}´`;
        str += `´+${application.diff.replacement}´${application.diff.suffix}`
    }

    document.getElementById("output-section").hidden = false;
    colorize(document.getElementById("output"), haskellKeywords, str);
}

async function copy() {
    await navigator.clipboard.writeText(document.getElementById("output").textContent);
}

function colorize(el, keywords = ["True", "False"], str = undefined) {
    str ??= el.innerText;

    const keywordPat = "^("+keywords.join("|")+")";
    const keywordRegex = new RegExp(keywordPat);
    const keywordStartRegex = new RegExp(keywordPat + "($|[^a-zA-Z0-9_\"])");

    let children = [];
    while(str.length) {
        let section;
        let style;
        let removeLen = -1;

        if(str.match(keywordStartRegex)) {
            section = str.match(keywordRegex)[0];
            style = "keyword";
        }
        else if(str.match(/^[a-zA-Z_]/)) {
            section = str.match(/^[a-zA-Z0-9_]+/)[0];
            style = "identifier";
        }
        else if(str.match(/^\d/)) {
            section = str.match(/^\d+/)[0];
            style = "number";
        }
        else if(str[0] === '"') {
            section = str.match(/^"(\\"|[^"])*"?/)[0];
            style = "string";
        }
        else if(str.startsWith("-- ")) {
            section = str.match(/^[^\r\n]+/)[0];
            style = "comment";
        }
        else if(str.match(/^´\+[^´]*´/)) {
            section = str.match(/^´[^´]*´/)[0];
            removeLen = section.length;
            section = section.substring(2, section.length - 1);
            style = "highlight";
        }
        else if(str.match(/^´-[^´]*´/)) {
            section = str.match(/^´[^´]*´/)[0];
            removeLen = section.length;
            section = section.substring(2, section.length - 1);
            style = "replaced";
        }
        else {
            section = str[0];
            style = null;
        }

        if(removeLen === -1)
            removeLen = section.length;
        if(!removeLen) {
            section = str[0];
            removeLen = 1;
            console.warn("Empty section selected with style", style);
        }

        str = str.substring(removeLen);
        if(style)
            children.push(colored(section, style));
        else if(children.length && typeof children[children.length - 1] === "string")
            children[children.length - 1] += section;
        else children.push(section);
    }

    let selection = getSelection();

    let p = selection.focusNode ?? document.body;
    while(p !== document.body) {
        if(p === el) break;
        p = p.parentElement;
    }
    if(p === document.body) {
        el.replaceChildren(...children);
        return;
    }

    let cursor = selection.focusOffset;
    let e = selection.focusNode;
    if(e === el) {
        cursor = 0;
        for(let i=0; i<selection.focusOffset; i++)
            cursor += el.childNodes[i].textContent.length;
    }
    else {
        if(e.parentElement.tagName === "SPAN")
            e = e.parentElement;
        for(e = e.previousSibling; e; e = e.previousSibling)
            cursor += e.textContent.length;
    }

    el.replaceChildren(...children);

    let range = document.createRange();
    for(e = el.childNodes[0]; e.textContent.length < cursor; e = e.nextSibling)
        cursor -= e.textContent.length;
    if(e instanceof HTMLElement)
        e = e.childNodes[0];
    range.setStart(e, cursor);
    selection.removeAllRanges();
    selection.addRange(range);
}

function colored(text, cls) {
    const el = document.createElement("span");
    el.innerText = text;
    el.className = cls;
    return el;
}
