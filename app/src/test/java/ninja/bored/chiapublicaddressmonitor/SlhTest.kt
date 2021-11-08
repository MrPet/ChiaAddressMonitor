@file:Suppress("SpellCheckingInspection")

package ninja.bored.chiapublicaddressmonitor

import com.google.gson.Gson
import java.util.Date
import junit.framework.TestCase
import ninja.bored.chiapublicaddressmonitor.helpers.AllTheBlocksApiHelper
import ninja.bored.chiapublicaddressmonitor.helpers.ChiaExplorerApiHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.ChiaExplorerAddressResponse
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import org.junit.Test

class SlhTest : TestCase() {

    companion object {
        val bigAddressArray = arrayOf(
            "wheat1wrxhy8qwtq52jzmljauh6uss444cnqhp7v3y7ut4qtpfegmgdtvq2rsd46",
            "wheat14uvpq7cv4avjet5kq6hhvzjglgnzfxl2qep0hdvvtz70p85dwz9sf2muf5",
            "xca1weuvrya2stlh576vmr87hk0hsgtfrywelklffergcakh9a8vuues20lutg",
            "xca1r24g5qascmy4c7qgngac0j7vmgc5g3qm5wwrsddwe4neq5k2znlsh4gyhg",
            "xvm1hf7kdraeaudz7rnjpsz4pa6jfgrt5wz3g0dfsc307jdmty8z7sgs2l400f",
            "xvm1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsameg2j",
            "trz1fmt2t6hwxwnzrkgu8mzrrr5m3gcty79nvkteut3y4d0jl4t7tg5suv2hj8",
            "trz1tds65recrnj6uup6l56p7t7mp2jmazs7vmuqxpcgqdx8znm3ajrstwrpd8",
            "trz19tetxgz07w7a3uwjfn8l5lfk0ptmxvdcndqwmefd0lztlfqhtdxs7z4fu3",
            "trz1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsx89zz5",
            "trz1us2neg6h3nf3e6gdxc5a5uqrxa3ezcxne30khlzs86c7x3vemuhqxu4u4z",
            "xth1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs8ywzee",
            "xth18cu8nw2ajwry2ajd9w587k2nyskazdqda2g932rz3tneeagstnysnjvlax",
            "xth1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs8ywzee",
            "xth1n9z005n2ym58fs7xxghepcyqeh62gf70fax7yza7lje4l97utc8sgaydns",
            "xth1ymzxzvwgcmdjamus3svum4z6hwfl577jms6qa69dys9l38x0smnq6de9mv",
            "tad1rm022nm90wqeqazxyhg26qaq7v5zxfyjg5efvmml0gz27smyrh3q2hzqke",
            "tad1aa83pxn2t4jmuw3llkkllsesvmhfqsxsh5cg0srn33stcksnfa9survz8y",
            "tad1v7edcxxk6wnh8da0jwe5z9ssuy3fs36m7hfqyfptaz97wl8s2jrsspfq7m",
            "tad1qcaya8j40yw0utekd0m29dddzvkhm7nw5aqy946w0efslw0htwmslaefeh",
            "tad1tfx6ga7jefefkpxk73nug2adxmtkucnjwcqxy0dnmc0l5cckffqs6yx6ta",
            "tad1eqd4mxstyg7x2k3ql7u6jahytr885tsmkm42wavwa4cpk67jsylqg3kz4x",
            "xtx1x54yj0ywg8ll8w6fpme8qaffnty7et9x53482nt97na429sqgf3sxnkx9j",
            "xtx1em978am6w8t4lzdc67p8azfy0r4w9e4e47x3a5p2r8rvswvy5k6qh5r0v9",
            "xtx1fr4mlauat87cas758r246ud9snuhy40upym8sxxv4tkmhsa28llsvhf5nj",
            "xtx1ay74zk3qysr7kg0ytn5zvf7k7y8lkel50cyjdw0eptxs2c0mm0usw6luav",
            "xtx1v9uwp53jth8uskptvxcqkw6vwhhmzl37q7xwjy7zy85t8pl5memqwp45pg",
            "xtx1em978am6w8t4lzdc67p8azfy0r4w9e4e47x3a5p2r8rvswvy5k6qh5r0v9",
            "xtx1x54yj0ywg8ll8w6fpme8qaffnty7et9x53482nt97na429sqgf3sxnkx9j",
            "xtx1ay74zk3qysr7kg0ytn5zvf7k7y8lkel50cyjdw0eptxs2c0mm0usw6luav",
            "xtx1fr4mlauat87cas758r246ud9snuhy40upym8sxxv4tkmhsa28llsvhf5nj",
            "xtx1v9uwp53jth8uskptvxcqkw6vwhhmzl37q7xwjy7zy85t8pl5memqwp45pg",
            "stor19esdhmgdxl57kdvqjmgt0xgsyj8y2vvl75fvqaletkc5nqzcjspsj7mjdr",
            "stor18u4auh44fcwfqaf6cj68unhxcx3qd8qxv7nnplrc0tl838v2cdzsp9cw03",
            "stor1eftcdzsp4kxynfuxqjghtn7tz09wg3fcpnqvl3eyclk0n0utwxxq43h940",
            "stai10rwjza0gwndgc9en3yutgqkcl9zwekwhnytzkrl0q0qd008mx52qgpmak5",
            "stai1h5kzf4d0zvqthmwnsmjwk4ar4ca3tdpq7n6php3de2ljfzcfzncsuuvqkl",
            "stai1mrjml9w39car0rk3nmnsrhs8awv06hty8c2nxv4e2a7ycaxvj9fqccngqg",
            "stai1pl8fjkmfdcgmymy2annv6cvy5q33r8k9rcj343nqkt2acdt8uulqzh444t",
            "spare1s66k54z2zhlzkja05zzlqa07l670w7ryrw37shks67etcjt7w9tsm3vpt9",
            "spare1qn45uj0rqx8fa5uvgyazmu3taskjem7389h0pmkcjyr9yuw4wkfsts8j7d",
            "spare1wjcy952gyzmnw3jzqcvqsup9l6hhg9pngvhxv0y0zwv4kdc0jexqt0tw88",
            "spare18fkw4ve89hq8a2sg60whscahfqfuy35gr44ycgw6hu28kjge74wstdm8q0",
            "sock13tfrwttgeqmn3us3l7y9hkklmt5efzq32e9xh6zwwcpnq7kgz96sc96pjd",
            "sock16uryrvt6zgld45grt045xz8ztvnkcf7aqk6er2a9a275s5je9m3s5szhvy",
            "sock10twg26nzh79z3uskf4ywwvfz9f2s5af3qknfk9cxyal032wuelrqmjf5eu",
            "sock13rs866z4lzc4anjyek42q3adxd6mhd3ce825xczl35d4v6qzsjuqm6j2vn",
            "xnt1u4dvussf9mykphnukg3g2tpdhnfm8lcsep6v2nmaupky0cgqfu7qd93eh4",
            "xnt1hd2n30txma5czkj04mrgneyvxuxd5hrn4pknunxtu5yhpn4jfeus8cxhmp",
            "xnt1yyyrd57fnm97w9ny0h8wy0ztzfxvh3ugzqrszn2fp3lrrlmp8teqxtvu6q",
            "xnt15u86w7e9c3nqayymqe3ayuhsxqvrdwh9ht6pf30epdhtczdu7kgqmq6tux",
            "xse19u2r46jadv7vqfwl435aqxk58auq3356p2um5nuv9n0f20a5m9ussjle23",
            "xse1538nqple3fgqy5e63wq7u4c99k5ger5se2qk8s4yzhf920xnn9gq4pzeg2",
            "xse1t3ev5wr47lexaqrg7ut5pgxytqpxksxd67q6stzvhw79q3wnr03s2qgw5t",
            "xsc1k5e8qyspfdpwx7d6ffqm8j7grup3h4925zwlmrhasz03403z0g6qhj4pvr",
            "xsc1mlgack4xff6rkevzt0qz9f42j3z926dxnus0nrpawrqjdynhj3yqhkj0ez",
            "xsc1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsx4j37v",
            "xsc1u2762frer79pp2pfnla5yfcdmgqh86euq6ec327xpqz5x5p99mgq4eck4g",
            "scm14uvpq7cv4avjet5kq6hhvzjglgnzfxl2qep0hdvvtz70p85dwz9sawh0su",
            "scm1rm022nm90wqeqazxyhg26qaq7v5zxfyjg5efvmml0gz27smyrh3q7kqcdj",
            "scm1x7ld2p0umqs3jce76wyc0vz0j650hmklf9cs2te6jtkn4dwqdeyq0tmn82",
            "scm1ckjwmjqgl74pagm02305luljqn8zh9xqgfu3yaj62v2gkypxqszqceagfw",
            "xslv1eftcdzsp4kxynfuxqjghtn7tz09wg3fcpnqvl3eyclk0n0utwxxqvt7fyk",
            "xslv1hu4j3wfmcn4y9503v083gallyrrxl3tc9y4s207r0vxep0rumzsquqks3y",
            "xslv1lyntvcem9072sz803p3h7rcfkwlsg4ks69cjlz0y87yx055lh0ssqspyxs",
            "xslv1w5nm8pxwtmtu0xlfrz5lvavnfkf7vj8pzma6mc82sj653nhvke4seznra3",
            "xcr19uhq9klky2wn5wammjrdmtlzqxl8n5dh3dv3ka84maysvethfwesat3gde",
            "xcr1pdf0xetkr0k4pppqwv0hslvldr2qlrem09c00ks9y097zufn8drq5hlprx",
            "xcr1hecyq39vlhtw58hd4cnmwwu6s7ctvylelqk25yclah2r2f54304q52g7np",
            "xcr19uhq9klky2wn5wammjrdmtlzqxl8n5dh3dv3ka84maysvethfwesat3gde",
            "xcr1ssks52ue5ecl62uh8rhg9mpgmad789z44ptrfdrqpdnfhrw7d4ks9kmtn7",
            "xcr1pdf0xetkr0k4pppqwv0hslvldr2qlrem09c00ks9y097zufn8drq5hlprx",
            "xcr1hecyq39vlhtw58hd4cnmwwu6s7ctvylelqk25yclah2r2f54304q52g7np",
            "xcr16nmaqpkc0uh4y60uyjyafh2gw5pg6hvjvu4ghnmpvhxnen0xdrwsyg5x2m",
            "xcr19uhq9klky2wn5wammjrdmtlzqxl8n5dh3dv3ka84maysvethfwesat3gde",
            "xcr1ssks52ue5ecl62uh8rhg9mpgmad789z44ptrfdrqpdnfhrw7d4ks9kmtn7",
            "xcr1pdf0xetkr0k4pppqwv0hslvldr2qlrem09c00ks9y097zufn8drq5hlprx",
            "xcr1hecyq39vlhtw58hd4cnmwwu6s7ctvylelqk25yclah2r2f54304q52g7np",
            "pips12nz5kpgv4g6f2dvur4j877hv4w8r628juq3lqfudfv7wd5drfrcs6anp32",
            "pips1rgz37v0z3x690vq8qnt242dz0yge9j7wry4uur0c50rs0uyfmekq2a663w",
            "pips14eac9w6vjrspy9yprnwnr7yzpepxeus3xmwzf70hgeayc8kjcpws2nf4hp",
            "pips1zdtn7yhfdh4lgn9yx4rel7ksl9urvgflsfnkjnvpsqw8gl65u29sgy9xu6",
            "pea1jpzkags97srqh4uq6ah3ps0st5g0ln4593xjt6g3lrwwtk3d0a3sx0ya65",
            "pea16uryrvt6zgld45grt045xz8ztvnkcf7aqk6er2a9a275s5je9m3sfv3cte",
            "pea1ckjwmjqgl74pagm02305luljqn8zh9xqgfu3yaj62v2gkypxqszq55r6g8",
            "pea1x7ld2p0umqs3jce76wyc0vz0j650hmklf9cs2te6jtkn4dwqdeyqrx9pxr",
            "pea1hecyq39vlhtw58hd4cnmwwu6s7ctvylelqk25yclah2r2f54304qwtwllg",
            "pea15zzfvwd38ysdfkeqn803hthwdk40xmf76rfhyrzvcvs744y4yc2qmde0hc",
            "xol1xff06jnuger5s6ewa47xqrmxm6z09rn8m2wg9qsdyf4cfd8uwrdsnhwkkt",
            "xol1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsmlay0e",
            "xol13ztwgtwpcqa6pqnz8cu8w6fd4vk0nx478x29dvzl70zd6csxfkzsmaw7lr",
            "xol1ymzxzvwgcmdjamus3svum4z6hwfl577jms6qa69dys9l38x0smnqxk2rdv",
            "nch1kqjpg3tyjm46wxyrw3t3c9zdsk7vvrd3fxman6zjav3s5yccqssslf6gfe",
            "nch1zhzvfdhs58kelhj6mrjk3wpl6qgarhrjvltte5rpm3xgfhwwfjpq643uvw",
            "nch1lstht5zmftdl0au59andwruntkve83dw75s8x35mmcq38g5g90lqpuqfje",
            "nch19gl4sjsl7jg5gfpsceglnc9nwq8l8acsru6rc2fzls4z2kj05lls8kyjds",
            "nch1kqjpg3tyjm46wxyrw3t3c9zdsk7vvrd3fxman6zjav3s5yccqssslf6gfe",
            "nch1zhzvfdhs58kelhj6mrjk3wpl6qgarhrjvltte5rpm3xgfhwwfjpq643uvw",
            "nch1lstht5zmftdl0au59andwruntkve83dw75s8x35mmcq38g5g90lqpuqfje",
            "nch19gl4sjsl7jg5gfpsceglnc9nwq8l8acsru6rc2fzls4z2kj05lls8kyjds",
            "mga14eac9w6vjrspy9yprnwnr7yzpepxeus3xmwzf70hgeayc8kjcpwsetzes8",
            "mga1mhld5f0lfe6s3f6l3u0h8wunmghzwfettwd2jsjk33v6qx885hmser8ksq",
            "mga1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshssptk6n",
            "mga1ym33qgz3udnevwydfc57t3d9rexrvdkc6vrmxrjhm4x8caudlavszyy085",
            "mga1q5eznzncs997vqqnq6ad7j0uc6yvuqgenp4uzzzx6cg3ax909upq5lzwce",
            "xkm15qur0lwwe6vvq08lql7gqag0zycavrvlp57znvt73774ahas6x8qseekuz",
            "xkm1l0qmkdzh5mf2pqw0adknr5lm502td9jk3zprkltcvusskqqutt0s947uyl",
            "xkm1ymzxzvwgcmdjamus3svum4z6hwfl577jms6qa69dys9l38x0smnqakeejh",
            "xkm1078cvkwvak6738027kedyex2sh0zmraswlvrkmvx3d08nx6yj68se3mq5v",
            "xkm1gnngsn766x5k7kdhr8gzq20gwc5gu6m8rezn0d2dwhcjuh7jw9usg8ucy8",
            "melon1nltkz32eq6extrgxs59vykg0lre9gjj0ljefjz67rvzl5gqzphss6v90q4",
            "melon1p603nre25gdfyw363tt6zv2alcnf2xj9rvk3ezk9zkt7cn0srxwqz4dy5m",
            "melon12d9yxhs5re29xjf57h5p0vp03z7eac604cgenj7c6t4qq82fsn7s8s3s30",
            "melon19n27y9hsyg20mswvdapeyneakg2hxsmglcz78dta4eatg5dhtczsvwr4a9",
            "xmx1ayuxrfn5aqnj3chy5rjhc3x8wj0qk392q3zxucry6uhq3lc90ueq552cvq",
            "xmx1p98pqgw2vr7n7heq0shaykkgakafjlluqrygst5qeca3q3vwrqzsh0x4rk",
            "xmx18qnsxle4fxpspdya9kla8n8jnnykdm87tcnkfjkmf0hjpks9gzqsjm3e6r",
            "xmx1p98pqgw2vr7n7heq0shaykkgakafjlluqrygst5qeca3q3vwrqzsh0x4rk",
            "xmz1slvtqaa705rtv2gj3v73m68e6fcusu0nzxamdp5t09lp4jvwlnfswx35dt",
            "xmz14x6hdt8w2ccwrpevfnrlp5m397d3kgelwuq2vh9ax44d6rjrxyfq9cx2wg",
            "xmz1dz0rw73msgk3v07s9wtqar0daw653ajj348r0qhv225x747gwzcqtrx394",
            "xmz18fkw4ve89hq8a2sg60whscahfqfuy35gr44ycgw6hu28kjge74wszaez3q",
            "xmz14rujavg950var4zyxnm8fd84n6mqmq2qsw4jyda69m6ng7ek5nlq2ayd6v",
            "xmz1sjj4n9mu3gnt33rfvszlex5ntvyqqq2j8luf9k0ashx4dgkcgykqzfeq05",
            "six1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsd78a38",
            "six1h9x6hsplvepz0vhq00sndf3t305kppn9zcn3zsc2rc6zhh6atzvqtd7wfm",
            "six1u4dvussf9mykphnukg3g2tpdhnfm8lcsep6v2nmaupky0cgqfu7qgxz6k2",
            "six1dyw33q7lfh7cxyu590yklntrvym30zkh2hcjaz5t2yltvsrxafnqxtk3n4",
            "six1hd8mytdm8tuz9wtf32ffcj4q53g0c4aqwazch6zvv37exw7yhz8scgtgwu",
            "lch1fceus5nuwrr0rl4yuvuaaectmrc7tnnh5p9m5xmd0vx6zedmq4ps57uzy8",
            "lch1ymzxzvwgcmdjamus3svum4z6hwfl577jms6qa69dys9l38x0smnqe20t0f",
            "lch1gk87f54ug28zvgcndtsur2zudpsuvdemc0gl3jyng7h6hl7phhlsslpr02",
            "lch1fceus5nuwrr0rl4yuvuaaectmrc7tnnh5p9m5xmd0vx6zedmq4ps57uzy8",
            "lch1ymzxzvwgcmdjamus3svum4z6hwfl577jms6qa69dys9l38x0smnqe20t0f",
            "llc1dwr8repfnthmzaaw2ucp4ehlg7l8mmc572gcf2uja2xf06rzxydswcaze0",
            "llc1gk87f54ug28zvgcndtsur2zudpsuvdemc0gl3jyng7h6hl7phhlsecm8v4",
            "llc18chqcrw350gqk6gj7x2v5vw6h7u703e0af84ne9ckuvrra56paasqh7v9l",
            "llc19tnxx4eh36vjqfrxyxm0583ln9prs3qk8eepdtmu4zuq62vvdursl2vkg8",
            "llc1yfsmp8t4fhgjnwyrpa5vkgvl0swcw07utz78lm6a06r7luj8e4uq004tvd",
            "xkj1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs089932",
            "xkj1kjtwgty9pnqt7hjxdpxcavfa2fk0wm806exctx4h4wn6thuw47lsley9vz",
            "xkj1rm022nm90wqeqazxyhg26qaq7v5zxfyjg5efvmml0gz27smyrh3qjjfl0j",
            "xkj1ymzxzvwgcmdjamus3svum4z6hwfl577jms6qa69dys9l38x0smnqjwjznl",
            "xkw1zhxdlam5anmkmrx264z25t2xu88gs29xu796e4cklcvlx4nxnumqjktr00",
            "xkw19tetxgz07w7a3uwjfn8l5lfk0ptmxvdcndqwmefd0lztlfqhtdxsajqukc",
            "xkw16dk7jncmflzw3teyagusq3a56q4jkaa5v7yjtugtpd8ydjl5l9usyansea",
            "xkw1u4dvussf9mykphnukg3g2tpdhnfm8lcsep6v2nmaupky0cgqfu7qq04s0s",
            "xka18ds3fw56wtttg7xm2d9s4wul720xr8ex50us54t3kz74ylc7avzs09cfdy",
            "xka1z2tsdgawqjnl0rc8stzfyh4x58wgxruq3klgy68g4phfhtnptu0sy0ju6r",
            "xka1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsfs3tp8",
            "xka14x6hdt8w2ccwrpevfnrlp5m397d3kgelwuq2vh9ax44d6rjrxyfqep7zg6",
            "hdd1ffpu0cjsy9z5vtf7awl9qauj8x832ln02gjt0x44gzz6ku00a7ss7lh3ay",
            "hdd10wn237sa0hkkn7tcj8ajhs3lfz3usmunns7eqct0wzejyfe0xh8snjgf4q",
            "hdd14qen0p9spqfu7fmmvg9m8gtapahg7hlnx3emzdp66uuy9hjwyresfza9q8",
            "hdd1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshswrjqpm",
            "gdog1aufn76wg2vgeejmjh3wefwwane6suaecnha7cj9k28u3l9plpp6q7wekl4",
            "gdog10qmxc4l5ek654pz4a38k74pc03fh46074zxcr4kagd93udykgulsdtlykq",
            "gdog1vq35x6vagy2qpy3htyzdexjf8tax6068tdxxtczurmsslgwm6rzsth6wfe",
            "ozt1nta4pt396k8s28eh6lhd33lzwp3f8gpl06ny3jws5w03za4vlq9qp98k8z",
            "ozt1v9uwp53jth8uskptvxcqkw6vwhhmzl37q7xwjy7zy85t8pl5memq77l90z",
            "ozt1wkr954rpx3f3kl04zz3zgvlx5umldp52spleqycxgvtrn354epdslhh6ju",
            "ozt18fkw4ve89hq8a2sg60whscahfqfuy35gr44ycgw6hu28kjge74wsumws52",
            "xgj1fjrvhherms2r9xa6znhfcglhwy34j9sm24nzl7097mpmcr56pfhqmmj9r4",
            "xgj1qsgw30u8r2gl43ztmgxghjvgwklk2824lyh8wh5seqhtgg7tc2cs6lwekh",
            "xgj1zmdmck8e3l3wuq47nhrhurw73ppcxazk0vd7cr8smjmw2f3e2cgq240czv",
            "xgj1pvmamkea93kf5aj0mjkpm2nzgskcr42l0r9hac3cr5nfxp3q4v5qzxwvha",
            "xgj1s66k54z2zhlzkja05zzlqa07l670w7ryrw37shks67etcjt7w9tsyj8226",
            "xfk1v33vjacft4lxt7yjedy84xcx9wzfnk9lgum7p7zdnx4zmc49vesqtzeqhx",
            "xfk1ay74zk3qysr7kg0ytn5zvf7k7y8lkel50cyjdw0eptxs2c0mm0usg89d0f",
            "xfk15zzfvwd38ysdfkeqn803hthwdk40xmf76rfhyrzvcvs744y4yc2qfw9wmt",
            "xfk17f4a08r9asf5xkzpfdq7w6gxp8247echpgl64m3gvjtgnv5z0anslsxvjj",
            "xfk15f5u5wncujyk947jk972e4ntcxadc5dpxa8xydhxumgz6cte9m2sjrc498",
            "xfl1mquddxja9v03fuu86qajt0m4d4gqccu54c0nqf77vrtq8fdtzcusuym5q6",
            "xfl1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsj492lc",
            "xfl1tdtupsp9t2skne3h8g27gsgwrwsgt4v3tn5x0ws2plg0fc7m3zas632m5m",
            "xfl1ygektqdmhwfg6eqrmwv8095z8n2tvvhwjg5xr6w8fhctaxrcpl7swxsg4w",
            "xfl1mquddxja9v03fuu86qajt0m4d4gqccu54c0nqf77vrtq8fdtzcusuym5q6",
            "xfl1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsj492lc",
            "xfl1tdtupsp9t2skne3h8g27gsgwrwsgt4v3tn5x0ws2plg0fc7m3zas632m5m",
            "xfl1ygektqdmhwfg6eqrmwv8095z8n2tvvhwjg5xr6w8fhctaxrcpl7swxsg4w",
            "xfx1zt5yzttmw70d3qsanjfynkyuz06ny3lljll53q4a3key29r2jfwq7nejyc",
            "xfx1vq35x6vagy2qpy3htyzdexjf8tax6068tdxxtczurmsslgwm6rzsqqkw63",
            "xfx165rsvv3p24vjwgujl9w6lgafz752r06n8pjekttttj7ytrjd4u5stlq4lg",
            "xfx1rtgzng3a2qlc25gx36c6d9fp3ghfx4wul3tjpe5rt62ma4p325gqvzutpc",
            "ffk18v253pgnu3sd2ze9s47w9szj2c4fvh9vh727869gjt956pc77nls23mtzq",
            "ffk1dfumudcykrmj9ejzs4j29jrf6sz33342y9auqhc4k45mmcdfpxuqvcq6ru",
            "ffk1v3uewj9meq2jrpd8l036snj25a4gx6r780l0ktfjxu5njh8685zs556pr6",
            "ffk1ufgnsvn7692dt0xgkgxm3xtm34lfuklcp9skvzzdkezcqvk9ac2qphhz0l",
            "ffk1n4tp5zc7xmd98ln0fy3qugwmkmzhwh0jvse0adykjkgsvn9t90jq4qmr2t",
            "xeq1j0zrzsac7s3w4vfxd85hf7asxyu8cufnjwrnqurnga7dwc00lg6q8ywfdx",
            "xeq1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsmgm6nj",
            "xeq1t4hfcf3xuuavjhe55854na0jm8yvxpazvf6anssp4rc3tyrn894q2zmc97",
            "xeq1r24g5qascmy4c7qgngac0j7vmgc5g3qm5wwrsddwe4neq5k2znlsdmyzph",
            "xeq15zzfvwd38ysdfkeqn803hthwdk40xmf76rfhyrzvcvs744y4yc2q0ts9kf",
            "xdg1sxjzs0mfljmlmsfms29y3lkm83rddzprf3t0le68t8ldwqpt5kusvyvz35",
            "xdg1fr4mlauat87cas758r246ud9snuhy40upym8sxxv4tkmhsa28llsp5et3y",
            "xdg1600se3z3xuxwvdrsg02v8umhcn237nmkw2fsztmnanesdx6z48usf6gk8n",
            "xdg16uryrvt6zgld45grt045xz8ztvnkcf7aqk6er2a9a275s5je9m3ss38hhe",
            "xcd1sjvr50fvh80tdpcuhplpwgc5g88kd304ttlullf975tfmfkw3uyscpfuej",
            "xcd1rgz37v0z3x690vq8qnt242dz0yge9j7wry4uur0c50rs0uyfmekqsv0dmt",
            "xcd1w2906l3esxhzlu3ypnssdhuz3n2t8angzuxw46relpng9y0uq5psx7f042",
            "xcd1s3psfzpw8kp9ktyf3sewlgdqwcm6vmpjdlc57gw4tn2hhnzzlsqqmrqc39",
            "xcd1jh0pg3frk44mtd825un94ar90myt00ey4vxz4jtjhrt8mzdxhgwqphwrnw",
            "cov1rgz37v0z3x690vq8qnt242dz0yge9j7wry4uur0c50rs0uyfmekqv5jkav",
            "cov1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs9sgk3h",
            "cov1lwfa3nftscn8xch3xvq5q7pm6e7t6jkkz4864xckn557epgkwynqaq0wqy",
            "cov1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs9sgk3h",
            "xcc15dqe3kydlhrqh9zkvnrs8w09nxmqjheljlvps7ty76nvzh9j88yqaallsh",
            "xcc1qgy6v0xzuvxn59k50z2lnmw8vh5ehq4cnpmavthv0gxaf3k90masv28sm8",
            "xcc1h42mjfq2w7vqktf67vaftncwc5c2zh004rqsua8pqlzclemec5zslepkwa",
            "xcc1uk5ql9tg5sg070w8zcgs42p4k4tr6sw7fcqhsx222nye8k0pdlsqeh5djz",
            "xcc1dgy6s8809uz5t9vh6rs5nkk6j8c38arwzg8vjqsdzprshwpgm24sptlcft",
            "xch16hz38tvs2m6hjg0rjpryfya2teqrml5j7ym2kzz4r38pz9gufrxs8ezcv9",
            "xch12c48rjy0n4m54glvyzvwme98xwg25cdmkv3m765ngl2meetqz5kqhc2n92",
            "xch1p6ud57snh9np96aus7s8gncu72gd6kuumeangcrqgdv605pwflxsxzxam4",
            "xch1f0ryxk6qn096hefcwrdwpuph2hm24w69jnzezhkfswk0z2jar7aq5zzpfj",
            "xch1jp6frj3ecddur7dxak3n7lq0j75ltquh2zyd44epdu0d6704y2hqyky5hf",
            "cgn1mr836k4jdkvaktf6s2n5tvdnemj37dusd200s95dd44fl3pku69qtgaq90",
            "cgn1qxdlgt3uzxj5q3kyehlwl3jec9kfcxmgh8cutwydt5ad397up8vs047u74",
            "cgn1rnsrextaqs327cq5mt6jxs6c2s60yafseqq8c3gg8n7fgcyfmr6s35udwx",
            "cgn1rwdvh3gq9v527f4s68s2dn87rng9e6kuv74q4qhl4azrj0rvrnfsl4dmym",
            "cgn16q42mwkm59w6a3lnsgnh45xdg3aeam5vck5jnz3f78d0fawxu72qaz9u85",
            "cans18fkw4ve89hq8a2sg60whscahfqfuy35gr44ycgw6hu28kjge74wsvu3m8u",
            "cans10twg26nzh79z3uskf4ywwvfz9f2s5af3qknfk9cxyal032wuelrqjgf3at",
            "cans18fkw4ve89hq8a2sg60whscahfqfuy35gr44ycgw6hu28kjge74wsvu3m8u",
            "cans1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshsmgp63f",
            "cans156pnwessjewxqal0f6ygdzvhfqgwgpjnqnv5uwkdh5hpnxlgh7zq83f63u",
            "cac1ay74zk3qysr7kg0ytn5zvf7k7y8lkel50cyjdw0eptxs2c0mm0us6vt2qx",
            "cac16uryrvt6zgld45grt045xz8ztvnkcf7aqk6er2a9a275s5je9m3sfyr7g9",
            "cac1j8nzqrn8rs4a8zn77gdvdm89usrxelysspg9z3qdxhjey2rnqdhq79phyg",
            "cac1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs0xqk3l",
            "cac1tscgl0y8626xrktksq2adtuf789vc0sktswvnhpmlzqkn2v9rg8s3537ds",
            "vag1fr4mlauat87cas758r246ud9snuhy40upym8sxxv4tkmhsa28lls2gsruq",
            "vag1wn8gwhf06zepht7d4jvn4f92a29ddxwuvn2e56l4upad7k9pl8cq0azg98",
            "vag1r24g5qascmy4c7qgngac0j7vmgc5g3qm5wwrsddwe4neq5k2znlstuj03z",
            "vag1g59swdk0ad4tws6gfwlfa5emu7273slv9ycl704dwnq4fugch3rqusevyz",
            "vag1zsgcfvwv0vzye4y8jv7ct80ed5trfx246eqxdy5se5qngj84n3vqcqxztx",
            "xbtc16wujpg0zkkd8j89squ5k5q0q6nn9rrrvng7s6vqrvr0rr2zuqtqqxdxssd",
            "xbtc1fqz74jj6chr6a29xlt78wjfj5ycg43247eptl2jndeph87hddams4u5m9f",
            "xbtc1sjvr50fvh80tdpcuhplpwgc5g88kd304ttlullf975tfmfkw3uysazd9qp",
            "xbtc1hd8mytdm8tuz9wtf32ffcj4q53g0c4aqwazch6zvv37exw7yhz8sfaap3c",
            "xbtc1l2nn2vc54sm3ce8wv3f2phke3wq8xslwtxnx5ztkuskpp2cf9kascxckf0",
            "xbtc1w5nm8pxwtmtu0xlfrz5lvavnfkf7vj8pzma6mc82sj653nhvke4s5wr3pl",
            "xbt1fr4mlauat87cas758r246ud9snuhy40upym8sxxv4tkmhsa28llsn8vzfs",
            "xbt1fgxkt9d4cqxfpl9hggzdjzhwe9t5hgy7l53yznkwf3lqvwhvdgpsjsjpvy",
            "xbt1w5nm8pxwtmtu0xlfrz5lvavnfkf7vj8pzma6mc82sj653nhvke4sv9rnet",
            "xbt1ak8y9xa2895w45khvnmvuftwqgn2srp5k7uwgs5tgfslpnw8e25s0vdk2t",
            "xbt10dpc50yjp8hpz0usmtu3amzs722d96rgkycq05spg4a0hxnmn0zskhfjvr",
            "xbr1fgxkt9d4cqxfpl9hggzdjzhwe9t5hgy7l53yznkwf3lqvwhvdgpszrflsj",
            "xbr1khlzs23fphz0ujse9yg8e6llhw5huhhjjf502lh63ghy7r0r49ms9xlje6",
            "xbr107lu97qjg577kpd06n7lvlnupcf26kez0tu4rs4825h4v4akcemqypluqx",
            "xbr1u7e2j48p6arcexuthenkpxwh70mur94fgttr0fj9gfk4u26ys5jq0g90w2",
            "xbr1fceus5nuwrr0rl4yuvuaaectmrc7tnnh5p9m5xmd0vx6zedmq4psywwxr6",
            "avo1gfv3l0mp8kdd70d4k9nypf72a6qgzhzatv69ksm8px09j2c4j0aqa6cde6",
            "avo1l2nn2vc54sm3ce8wv3f2phke3wq8xslwtxnx5ztkuskpp2cf9kashhgeah",
            "avo18fkw4ve89hq8a2sg60whscahfqfuy35gr44ycgw6hu28kjge74wsyw36vw",
            "avo16eh0kjv897ysd9vg9332gwu87q4plp3707mann4h47zk52aqkp3qfk9mzd",
            "apple1dj8wfq07c9y8mgad0dq0dd5u6tuz37jw7jp4z6w3kjln7kq3ursqwfvprj",
            "apple1a5r057shkweqtpa7wkrsf6e0lq263lth50l20ktyua6hqf6hpshs8gne48",
            "apple1w5nm8pxwtmtu0xlfrz5lvavnfkf7vj8pzma6mc82sj653nhvke4s0dpu6m",
            "apple1hlm64950e38dz26sy9qx9j8yyz4gcwy0fwp7lgg839qfwjvjuvws9tn69k",
            "apple1njkjc7756ehmesttj8pssf7eaargnn48gyns98407kn54hvjluzqgdhws2",
            "aec16uryrvt6zgld45grt045xz8ztvnkcf7aqk6er2a9a275s5je9m3s0l6r0d",
            "aec1r24g5qascmy4c7qgngac0j7vmgc5g3qm5wwrsddwe4neq5k2znlslwxnyj",
            "aec1j8pkuunf7rzlxz5xnsapds54stqf4st4ndrx9ast0wu80ek8x53sxu5jar",
            "aec1elxntk4hg8yln62t75mm288lquaj7cdvlgt34j9d4jhvnjjfz26q89scwk",
            "aec1n8lv8rwmrs9vfknx57d7u0qwnt3g76p5qym382h5xevdk0tg4lts2fzel4",
            "ach1t9356a6wtxu9ry2eng4xqlx76gv6ev6xypnhnhfun6x9djq793jqawj2vg",
            "ach1wejs6jepzevvm8tl8vvpu3zs8gpujerhvfrftc87qwht935kwukqdzhdxg",
            "ach10qyddqdjnesenhq43csdplemnnh7psvldd0f8zjrwe2trkv4505qtdplev",
            "ach128tfx0n3rl2wjesh6yz86jp8qpxsyga3hy2l7fla32r74tvtu7pqf3alwj",
            "ach128tfx0n3rl2wjesh6yz86jp8qpxsyga3hy2l7fla32r74tvtu7pqf3alwj"
        )
    }

    @Test
    fun testChiaAddressValidityInvalidInput() {
        assertFalse("Address Null - invalid", Slh.isChiaAddressValid(null))
        assertFalse("Address Empty - invalid", Slh.isChiaAddressValid(""))
        assertFalse("Address Empty spaces - invalid", Slh.isChiaAddressValid("   "))
        assertFalse(
            "Address wrong starting characters - invalid",
            Slh.isChiaAddressValid("fxh1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v")
        )
        assertFalse(
            "Address too long - invalid",
            Slh.isChiaAddressValid("xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2")
        )
        assertFalse(
            "Address too short - invalid",
            Slh.isChiaAddressValid("xch1xntpeve5yjnadgjsyhvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2")
        )
        assertFalse(
            "Address invalid characters - invalid",
            Slh.isChiaAddressValid("xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv&d2v3qfvsvj097sywls7m6k2")
        )
    }

    @Test
    fun testBuildUrlFromAddress() {
        assertEquals(
            "https://api.alltheblocks.net/chia/address/xch1away45w2acy8cqgcjxnne8aket33y49tt437gjjk86y7fanstw7qyewsrf",
            AllTheBlocksApiHelper
                .buildUrlFromAddress("xch1away45w2acy8cqgcjxnne8aket33y49tt437gjjk86y7fanstw7qyewsrf")
        )
    }

    @Test
    fun testGetCurrencyIdentifierFromAddress() {
        assertEquals(
            "chia",
            Slh.getCurrencyIdentifierFromAddress("xch1away45w2acy8cqgcjxnne8aket33y49tt437gjjk86y7fanstw7qyewsrf")
        )
    }

    @Test
    fun testIsChiaOrForkAddressValid() {

        bigAddressArray.forEach {
            assertTrue("This is a valid address: $it", Slh.isChiaOrForkAddressValid(it))
        }
    }

    @Test
    fun testpParseApiResponseToWidgetDataInvalid() {
        val address = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv&d2v3qfvsvj097sywls7m6k2"
        val date = Date()
        val highAmountChiaWidgetData = WidgetData(address, 18375000.0, date, 18475000.0)
        // maybe change datattypes in future, but for now it is good enough wont show so many decimals
        val highAmountParsedWidgetData = ChiaExplorerApiHelper.parseApiResponseToWidgetData(
            address,
            Gson().fromJson(
                "{\n" +
                        "    \"grossBalance\": 18475000000000010000,\n" +
                        "    \"netBalance\": 18375000000000010000\n" +
                        "}",
                ChiaExplorerAddressResponse::class.java
            ),
            date
        )
        assertFalse(
            "Realy small difference compare " +
                    "${highAmountChiaWidgetData.chiaAmount} == ${highAmountParsedWidgetData.chiaAmount}",
            highAmountChiaWidgetData.chiaAmount == highAmountParsedWidgetData.chiaAmount
        )
    }

    @Test
    fun testpParseApiResponseToWidgetData() {
        val address = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv&d2v3qfvsvj097sywls7m6k2"
        val zeroAmount = 0.0
        val date = Date()
        val chiaExplorerAddressResponse =
            ChiaExplorerAddressResponse(zeroAmount, zeroAmount)
        val chiaWidgetData = WidgetData(address, zeroAmount, date, zeroAmount)
        val parsedWidgetData =
            ChiaExplorerApiHelper.parseApiResponseToWidgetData(
                address,
                chiaExplorerAddressResponse,
                date
            )
        assertEquals(
            "Api Response Netbalance with Zero must match widget data initialized with zero",
            chiaWidgetData,
            parsedWidgetData
        )
        val highAmountChiaWidgetData = WidgetData(address, 18375000.0, date, 18475000.0)
        val highAmountParsedWidgetData = ChiaExplorerApiHelper.parseApiResponseToWidgetData(
            address,
            Gson().fromJson(
                "{\n" +
                        "    \"grossBalance\": 18475000000000000000,\n" +
                        "    \"netBalance\": 18375000000000000000\n" +
                        "}",
                ChiaExplorerAddressResponse::class.java
            ),
            date
        )
        assertEquals(
            "High Api Response Netbalance must equal widget data initialisation",
            highAmountChiaWidgetData,
            highAmountParsedWidgetData
        )
        val decimalAmountChiaWidgetData = WidgetData(address, 28.1234, date, 28.2234)
        val smallAmountParsedWidgetData = ChiaExplorerApiHelper.parseApiResponseToWidgetData(
            address,
            Gson().fromJson(
                "{\n" +
                        "    \"grossBalance\": 28223400000000,\n" +
                        "    \"netBalance\": 28123400000000\n" +
                        "}",
                ChiaExplorerAddressResponse::class.java
            ),
            date
        )
        assertEquals(
            "High Api Response Netbalance must equal widget data initialisation",
            decimalAmountChiaWidgetData,
            smallAmountParsedWidgetData
        )
    }

    @Test
    fun testChiaAddressValidityValidInput() {
        assertTrue(
            "Address is valid",
            Slh.isChiaAddressValid("xch16g76z3545xy2u4cgm52jyc7ymwyravn7m6unv9udfkvghreuuh7qa9cvfl")
        ) // chia network 1
        assertTrue(
            "Address is valid",
            Slh.isChiaAddressValid("xch1qhgp3ytyauptzyv5p48gnqpmkes6u2sf8llc7m3eurcpg3emg9yqzzptac")
        ) // donation address
    }
}
