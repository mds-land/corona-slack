Coronavirus (COVID-19) statistics for Slack

COVID-19 datasource: https://corona.lmao.ninja 

## Features

- Send statistics by `/corona [contries]` where `[countries]` is comma separated list of contries
  
  Supported country formats in any case:
    - ISO codes (2 or 3 symbols)
    - Fully-qualified names
    
  *Countries list can be omitted, then app will send statistics with top infected countries*
  
   ![Coronavirus Statistics Example](./media/stats.png)

- Send statistics chart by `/corona-chart [countries]` where `[countries]` is comma separated list of contries
  
  Supported country formats in any case:
    - ISO codes (2 or 3 symbols)
    - Fully-qualified names
    
  *Countries list can be omitted, then app will send statistics with top infected countries*
  
   ![Coronavirus Statistics Example](./media/chart.png)

- Send statistics on cron expression basis
    
    Daily statistics for your team can be enabled when go to https://corona.mdsina.ru/admin/manage
    You will be logged through Slack OAuth2.
    
    *Important note:* Slack App must be installed to your team and you must be in the team to allow to configure statistics. \
    Changed configuration will be applied at least 20 second after saving.
    
    ![Coronavirus Config Example](./media/configure.png)

<a href="https://slack.com/oauth/v2/authorize?client_id=4928296994.211284940768&scope=chat:write,commands,im:write,incoming-webhook,mpim:write&redirect_uri=https%3A%2F%2Fcorona.mdsina.ru%2Fslack%2Finstall"><img alt="Add to Slack" height="40" width="139" src="https://platform.slack-edge.com/img/add_to_slack.png" srcset="https://platform.slack-edge.com/img/add_to_slack.png 1x, https://platform.slack-edge.com/img/add_to_slack@2x.png 2x"></a>
<a href="https://slack.com/oauth/v2/authorize?user_scope=identity.basic,identity.team&client_id=4928296994.211284940768&redirect_uri=https%3A%2F%2Fcorona.mdsina.ru%2Foauth%2Fcallback%2Fslack"><img src="https://api.slack.com/img/sign_in_with_slack.png" /></a>