# GitHub Contributors

API utilizando o [framework Play](https://www.playframework.com/) em Java para buscar a lista de usuários e suas contribuições em todos os repositórios de uma dada empresa.

A aplicação responde na URI `/api/v1/organizations/{orgName}/contributors`, substituindo `{orgName}` pelo login da organização a ser consultada no GitHub (teste com `github`).

Para executar localmente, é necessário definir a variável de ambiente GH_TOKEN com um token para acesso à [API do Github](https://docs.github.com/en/rest).
