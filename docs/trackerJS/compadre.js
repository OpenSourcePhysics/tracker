function OSPgetURLP(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [, ""])[1].replace(/\+/g, '%20')) || null
}

function hideshow(id) {
    if (!document.getElementById) return;
    if (document.getElementById(id).style.display == "none") {
        document.getElementById(id).style.display = "";
    } else {
        document.getElementById(id).style.display = "none";
    }
}