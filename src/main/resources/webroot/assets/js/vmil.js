$(document).ready(function () {
  $(".button-collapse").sideNav(
  );
  var md = window.markdownit({
    linkify: true,
    typographer: true,
    html: true,
    breaks: true
  });

  $(".markdown-text").each(function (i, obj) {
    $.get(obj.innerText, function (markdown) {
      obj.innerHTML = md.render(markdown);
      $(obj).fadeIn(250);
    }, "text");
  });
});
